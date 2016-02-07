import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;


/**
 * Created by chenhao on 2/5/16.
 */

public class PaidRankPipeline implements Pipeline {


    @Override
    public void process(ResultItems resultItems, Task task) {

        List appIdList = resultItems.get("paidIdList");
        AppInfoController appInfoController=new AppInfoController(appIdList);
        appInfoController.fetchStart();

    }
}
