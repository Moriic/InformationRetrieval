package com.cwc.IR.utils;

import com.cwc.IR.model.THUResponse;
import com.cwc.IR.model.THUTeacher;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

public class GetDocsUtil {

    public static List<THUTeacher> THUTeachers;

    static String HIT_URL = "http://cs.hitsz.edu.cn/szll1.htm";
    static String SAVE_PATH = System.getProperty("user.dir") + "\\src\\main\\resources";

    public static void main(String[] args) throws IOException {
//        getHITDocs();
        getTHUDocs();
    }


    public static void saveToFile(StringBuilder content, String school, String teacher) {
        File file = new File(SAVE_PATH + "\\" + school + "\\" + teacher + ".txt");
        // Create directories if they do not exist
        file.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content.toString().replaceAll("(?m)^[ \t]*\r?\n", ""));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getHITDocs() throws IOException {
        // 发生请求
        Document document = Jsoup.connect(HIT_URL).get();

        Elements teacherCards = document.select("div.teacher-card a");
        Element card = teacherCards.get(0);
//        for (Element card : teacherCards) {
        // 获取<a>标签中的href属性
        String href = card.attr("href");
        Document teacher = Jsoup.connect(href).get();
        String tid = teacher.select(".teacher-body").attr("data-tid");
        // 设置POST请求的参数
        Connection.Response response = Jsoup.connect("https://faculty.hitsz.edu.cn/TeacherHome/teacherBody.do")
                .data("id", tid)
                .header("Accept", "*/*")
                .method(Connection.Method.POST)
                .execute();
        String html = "<html><head></head><body>" + response.body().substring(1, response.body().length() - 1) + "</body></html>";
        Document doc = Jsoup.parse(html);
        System.out.println(doc);
//        Elements select = doc.select("li");
//        System.out.println(select);
//        System.out.println(doc);
//        }

    }


    public static void getTHUDocs() {
        // URL to send the POST request
        String url = "https://www.sigs.tsinghua.edu.cn/_wp3services/generalQuery?queryObj=teacherHome";

        // Request body
        String body = "siteId=3&pageIndex=1&rows=999&conditions=%5B%7B%22conditions%22%3A%5B%7B%22field%22%3A%22published%22%2C%22value%22%3A%221%22%2C%22judge%22%3D%22%3D%22%7D%5D%7D%5D&orders=%5B%7B%22field%22%3A%22firstLetter%22%2C%22type%22%3A%22asc%22%7D%5D&returnInfos=%5B%7B%22field%22%3A%22title%22%2C%22name%22%3A%22title%22%7D%2C%7B%22field%22%3A%22headerPic%22%2C%22name%22%3A%22headerPic%22%7D%2C%7B%22field%22%3A%22career%22%2C%22name%22%3A%22career%22%7D%2C%7B%22field%22%3A%22cnUrl%22%2C%22name%22%3A%22cnUrl%22%7D%2C%7B%22field%22%3A%22phone%22%2C%22name%22%3A%22phone%22%7D%2C%7B%22field%22%3A%22email%22%2C%22name%22%3A%22email%22%7D%2C%7B%22field%22%3A%22fax%22%2C%22name%22%3A%22fax%22%7D%2C%7B%22field%22%3A%22exField1%22%2C%22name%22%3A%22exField1%22%7D%2C%7B%22field%22%3A%22exField2%22%2C%22name%22%3A%22exField2%22%7D%2C%7B%22field%22%3A%22exField3%22%2C%22name%22%3A%22exField3%22%7D%2C%7B%22field%22%3A%22exField4%22%2C%22name%22%3A%22exField4%22%7D%2C%7B%22field%22%3A%22exField5%22%2C%22name%22%3A%22exField5%22%7D%2C%7B%22field%22%3A%22exField7%22%2C%22name%22%3A%22exField7%22%7D%2C%7B%22field%22%3A%22exField8%22%2C%22name%22%3A%22exField8%22%7D%2C%7B%22field%22%3A%22exContent14%22%2C%22name%22%3A%22exContent14%22%7D%5D&articleType=1&level=1";
        // 计数器
        int count = 0;
        try {
            // Sending the POST request
            Connection.Response response = Jsoup.connect(url)
                    .requestBody(body)
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)  // To accept the response as JSON
                    .execute();

            // 获取响应的 JSON 字符串
            Gson gson = new Gson();

            // 定义要解析的目标类型
            Type teacherDataType = new TypeToken<THUResponse>() {
            }.getType();

            // 将JSON字符串解析为TeacherData对象
            THUResponse teacherData = gson.fromJson(response.body(), teacherDataType);

            THUTeachers = teacherData.getData();
//            THUTeacher teacher = THUTeachers.get(10);
            // 获取teacher对象数组
            for (THUTeacher teacher : THUTeachers) {
                teacher.setHeaderPic("https://www.sigs.tsinghua.edu.cn" + teacher.getHeaderPic());
                StringBuilder teacherStringBuilder = new StringBuilder();
                String teacherHome = teacher.getCnUrl();

                Document document = Jsoup.connect(teacherHome).get();
                Elements tabList = document.select(".tab-list > li");
                Elements email = document.select(".email");

                if (!email.isEmpty()) {
                    teacher.setEmail(email.get(0).text());
                }

                if (tabList.isEmpty()) {
                    continue;
                }

                String[] tabHead = {"<个人简历>\n", "<教学>\n", "<研究领域>\n", "<研究成果>\n", "<奖励荣誉>\n"};
                String[] tabTail = {"</个人简历>\n", "</教学>\n", "</研究领域>\n", "</研究成果>\n", "</奖励荣誉>\n"};
                for (int i = 0; i < tabList.size(); i++) {
                    StringBuilder tabStringBuilder = new StringBuilder(tabHead[i]);
                    Element element = tabList.get(i);
                    if (element.select(".con ").text().isEmpty()) continue;
                    appendTextWithNewlines(tabStringBuilder, tabList.get(i));
                    if (tabStringBuilder.toString().equals(tabHead[i])) continue;
                    tabStringBuilder.append(tabTail[i]);
                    teacherStringBuilder.append(tabStringBuilder);
                }
                teacherStringBuilder.insert(0, gson.toJson(teacher) + "\n");
                saveToFile(teacherStringBuilder, "THU", teacher.getTitle());
                count++;
                if (count % 20 == 0)
                    System.out.println("爬取 " + count + " 个文档完毕！");
            }
            System.out.println("共爬取 " + count + " 个文档！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void appendTextWithNewlines(StringBuilder builder, Node node) {
        node.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node instanceof TextNode) {
                    TextNode textNode = (TextNode) node;
                    builder.append(textNode.text());
                } else if (node.nodeName().equals("p") || node.nodeName().equals("div") || node.nodeName().matches("h[1-6]")) {
                    // Append a newline before starting a new block element (paragraph, div, or header)
                    if (builder.length() > 0) {
                        builder.append('\n');
                    }
                }
            }

            @Override
            public void tail(Node node, int depth) {
                if (node.nodeName().equals("p") || node.nodeName().equals("div") || node.nodeName().matches("h[1-6]")) {
                    // Append a newline after ending a block element
                    builder.append('\n');
                }
            }
        });
    }

}
