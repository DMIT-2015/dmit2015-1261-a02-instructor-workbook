package dmit2015.model;

import net.datafaker.Faker;
import lombok.Data;

@Data
public class TodoItem {

    private  Long id;
    private String Name;
    private boolean complete;
    private Integer version;

    public  static TodoItem of(Faker faker){
        TodoItem newItem = new TodoItem();
        newItem.setName("Watch " + faker.breakingBad().episode());
        newItem.setComplete(false);
        return  newItem;

    }




}
