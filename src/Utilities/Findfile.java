package Utilities;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Findfile {
    public boolean findFile(Pattern  name, File file)
    {
        File[] list = file.listFiles();
        if(list!=null)
            for (File fil : list)
            {
                Matcher matcher = name.matcher(fil.getName());
                if (fil.isDirectory())
                {
                    findFile(name,fil);
                }
                else if (matcher.find())
                {
                    return true;
                }
            }
        return false;
    }
}
