// START OF FILE: Anotacao.java
package model;

public class Anotacao {
    private int id;
    private String titulo;
    private String descricao;
    private String status; // Usado para Kanban (AFazer, Fazendo, Feito). Pode ser vazio para Comum.
    private int listId; // ID da lista a que pertence (de user_lists)
    private String prioridade;
    private boolean isConcluidaVisual; // Persistido no BD para o estado do checkbox

    // Construtor para criar novas anotações (sem ID, com isConcluidaVisual padrão)
    public Anotacao(String titulo, String descricao, String status, int listId, String prioridade, boolean isConcluidaVisual) { // <-- NOVO PARÂMETRO AQUI
    this.titulo = titulo;
    this.descricao = descricao;
    this.status = status;
    this.listId = listId;
    this.prioridade = prioridade;
    this.isConcluidaVisual = isConcluidaVisual; // <-- ATRIBUI O VALOR PASSADO
}

// Construtor completo (para carregar do BD) - este já estava certo com 7 parâmetros
    public Anotacao(int id, String titulo, String descricao, String status, int listId, String prioridade, boolean isConcluidaVisual) {
    this(titulo, descricao, status, listId, prioridade, isConcluidaVisual); // Chama o construtor acima com todos os valores
    this.id = id;
    // this.isConcluidaVisual = isConcluidaVisual; // Essa linha não é mais necessária aqui, pois o construtor acima já faz
}
    // Getters
    public int getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getStatus() { return status; }
    public int getListId() { return listId; }
    public String getPrioridade() { return prioridade; }
    public boolean isConcluidaVisual() { return isConcluidaVisual; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public void setStatus(String status) { this.status = status; }
    public void setListId(int listId) { this.listId = listId; }
    public void setPrioridade(String prioridade) { this.prioridade = prioridade; }
    public void setConcluidaVisual(boolean concluidaVisual) { this.isConcluidaVisual = concluidaVisual; }
}
// END OF FILE: Anotacao.java