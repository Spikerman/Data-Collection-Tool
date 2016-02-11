import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;
import java.util.Set;

/**
 * Created by chenhao on 2/11/16.
 */
public class ReviewPagePipeline implements Pipeline {
    @Override
    public void process(ResultItems resultItems, Task task) {

        Set resultSet = resultItems.get("results");
        System.out.println("*******************************************************");
        System.out.println("-------------------------------------------------------");
        System.out.println("final total number: " + resultSet.size());
        System.out.println("-------------------------------------------------------");

    }
}
