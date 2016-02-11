import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by chenhao on 2/8/16.
 */
public class Toolkit {
    public static List splitArray(List array, int subSize) {
        int count = array.size() % subSize == 0 ? array.size() / subSize : array.size() / subSize + 1;

        List<List> subAryList = new ArrayList();

        for (int i = 0; i < count; i++) {
            int index = i * subSize;

            List list = new ArrayList();
            int j = 0;
            while (j < subSize && index < array.size()) {
                list.add(array.get(index++));
                j++;
            }

            subAryList.add(list);
        }

        return subAryList;
    }

    public static List<String> removeDuplicate(List originalList) {
        HashSet<String> hashSet = new HashSet<>();
        List<String> newList = new ArrayList<>();
        for (Iterator iterator = originalList.iterator(); iterator.hasNext(); ) {
            String element = (String) iterator.next();
            if (hashSet.add(element)) {
                newList.add(element);
            }
        }
        originalList.clear();
        originalList.addAll(newList);
        return originalList;
    }

    public static Date chineseDateConvert(String dateString) {
        Date date=null;
        try {
            String year = dateString.substring(0, 4);
            String month = dateString.substring(5, 7);
            String day = dateString.substring(8, 10);
            String result = year + "." + month + "." + day;
            DateFormat formatterCH = new SimpleDateFormat("yyyy.MM.dd");
            date = formatterCH.parse(result);
        }catch(ParseException e){
            e.printStackTrace();
        }
        return date;
    }
}
