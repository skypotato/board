package org.loveornot.board;

/**
 * Created by hunso on 2016-10-03.
 */
public class Board {
    String no;
    String title;
    String content;
    String date;
    String hit;
    String id;
    String name;
    String password;
    String type;

    public Board(String title, String name, String hit, String no) {
        this.title = title;
        this.name = name;
        this.hit = hit;
        this.no = no;
    }

    public Board(String content, String date, String hit, String id, String name, String no, String password, String title, String type) {
        this.content = content;
        this.date = date;
        this.hit = hit;
        this.id = id;
        this.name = name;
        this.no = no;
        this.password = password;
        this.title = title;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHit() {
        return hit;
    }

    public void setHit(String hit) {
        this.hit = hit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
