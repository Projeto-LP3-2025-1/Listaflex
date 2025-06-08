package model;

public class Anotacao {
    private int id;
    private String titulo;
    private String descricao;
    private String status;
    private int listId;
    private String prioridade; // <-- NOVA PROPRIEDADE para prioridade

    public Anotacao(String titulo, String descricao, String status, int listId, String prioridade) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.status = status;
        this.listId = listId;
        this.prioridade = prioridade; // Inicializa prioridade
    }

    public Anotacao(int id, String titulo, String descricao, String status, int listId, String prioridade) {
        this(titulo, descricao, status, listId, prioridade);
        this.id = id;
    }

    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getStatus() { return status; }
    public int getListId() { return listId; }
    public String getPrioridade() { return prioridade; } // <-- NOVO GETTER para prioridade

    public void setId(int id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setStatus(String status) { this.status = status; }
    public void setListId(int listId) { this.listId = listId; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; } // <-- NOVO SETTER para prioridade
}