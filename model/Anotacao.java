package model;

public class Anotacao {
    private int id;
    private String titulo;
    private String descricao;
    private String status;

    public Anotacao(String titulo, String descricao, String status) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.status = status;
    }

    public Anotacao(int id, String titulo, String descricao, String status) {
        this(titulo, descricao, status);
        this.id = id;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getStatus() { return status; }

    public void setId(int id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setStatus(String status) { this.status = status; }
}