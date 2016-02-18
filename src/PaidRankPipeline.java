import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.List;


/**
 * Created by chenhao on 2/5/16.
 */

public class PaidRankPipeline implements Pipeline {

    AppInfoController appInfoController;

    public PaidRankPipeline(AppInfoController appInfoController) {
        this.appInfoController = appInfoController;
    }

    public AppInfoController getAppInfoController() {
        return appInfoController;
    }

    public void setAppInfoController(AppInfoController appInfoController) {
        this.appInfoController = appInfoController;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {

        List appIdList = resultItems.get("paidIdList");
        appInfoController.appendAppIdList(appIdList);

    }

}
