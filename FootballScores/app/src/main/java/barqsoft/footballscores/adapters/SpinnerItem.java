package barqsoft.footballscores.adapters;

public class SpinnerItem {

    public static final int INVALID_ITEM = -1;
    private int id;
    private String value;

    public SpinnerItem(){
        this.id = INVALID_ITEM;
        this.value = "";
    }

    public SpinnerItem(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return this.id;
    }

    public void setValue(String value){
        this.value = value;
    }

    public String getValue(){
        return this.value;
    }
}
