import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by chenhao on 2/5/16.
 */

public class AppStorePaidRankPipeline implements Pipeline {

    AppInfoController appInfoController;

    public AppStorePaidRankPipeline(AppInfoController appInfoController) {
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
        List<AppData> appDataList = new LinkedList<>();
        int i = 1;
        for (Object appId : appIdList) {
            appDataList.add(new AppData(appId.toString(), i++, AppData.topPaid));
        }
        appInfoController.appendAppIdList(appIdList);
        appInfoController.appendAppDataList(appDataList);
    }

}
