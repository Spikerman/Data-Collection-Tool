import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;

/**
 * Created by chenhao on 2/7/16.
 */
public class UpRankPipeline implements Pipeline {

    @Override
    public void process(ResultItems resultItems, Task task) {

        List appIdList = resultItems.get("upIdList");
        AppInfoController appInfoController=new AppInfoController(appIdList);
        appInfoController.fetchStart();

    }
}
