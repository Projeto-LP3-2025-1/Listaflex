package model;

public class Anotacao {
    private int id;
    private String titulo;
    private String descricao;
    private String status;
    private int userId; // Nova propriedade para o ID do usuário

    public Anotacao(String titulo, String descricao, String status, int userId) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.status = status;
        this.userId = userId;
    }

    public Anotacao(int id, String titulo, String descricao, String status, int userId) {
        this(titulo, descricao, status, userId);
        this.id = id;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getStatus() { return status; }
    public int getUserId() { return userId; }

    public void setId(int id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setStatus(String status) { this.status = status; }
    public void setUserId(int userId) { this.userId = userId; }
}