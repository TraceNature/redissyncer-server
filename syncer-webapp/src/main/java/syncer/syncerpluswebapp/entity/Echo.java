package syncer.syncerpluswebapp.entity;

public class Echo {
    private final long id;
    private final String content;

    public Echo(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return this.id;
    }

    public String getContent() {
        return this.content;
    }
}
