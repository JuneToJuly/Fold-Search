package thomas.gian;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Locale;

/**
 nnoremap fo :action AceAction<CR>
 nnoremap fp :action FindInPath<CR>

 nnoremap ff :action thomas.gian.FoldSearch<CR>w
 nnoremap fj :action thomas.gian.FoldSearchCursor<CR>
 nnoremap fb :action thomas.gian.FoldSearchPrevious<CR>
 nnoremap fw :action thomas.gian.FoldSearchForward<CR>
 nnoremap fn :action thomas.gian.FoldSearchFrameAsMutator<CR>
 nnoremap fm :action thomas.gian.FoldSearchFrameAsAccessor<CR>
 nnoremap fu :action thomas.gian.FoldSearchToggleMethodScoping<CR>
 nnoremap fh :action thomas.gian.FoldSearchMutator<CR>
 nnoremap fl :action thomas.gian.FoldSearchAccessor<CR>
 nnoremap fa :action thomas.gian.FoldSearchAndAtCursor<CR>
 nnoremap fe :action thomas.gian.FoldSearchOrAtCursor<CR>
 nnoremap f; :action thomas.gian.FoldSearchFrameAsWindow<CR>
 nnoremap fv :action thomas.gian.FoldSearchFrameAsBlock<CR>

 nnoremap fk :action thomas.gian.WindowSearch<CR>w
 nnoremap fi :action thomas.gian.BlockSearch<CR>w
 nnoremap n :action thomas.gian.FoldSearchToggle<CR>
 nnoremap fc :action thomas.gian.FoldSearchToggleComments<CR>
 vnoremap za :action thomas.gian.exile.ExileAdd<CR><esc>
 nnoremap za :action thomas.gian.exile.ExileAdd<CR>
 nnoremap zx :action thomas.gian.exile.ExileClear<CR>
 */
public class JavaPsi {
    private static String windowSearch = "windowSearch";
    public static String getWindowSearchCursor() {
        return windowSearchCursor;
    }

    public static void setWindowSearchCursor(String windowSearchCursor) {
        JavaPsi.windowSearchCursor = windowSearchCursor;
    }
    private static String windowSearchCursor = "windowSearchCursor";
    public static void f()
    {
        int green = 0;
        if(true)
        {
            green = 123;
            if(true);
            {
                System.out.print(green);
            }
            if(true);
            {
                System.out.print(100);
            }
            int purple = 0;
            System.out.print(green);
        }
        int blue = 0;
        int red = 0;
        int orange = 0;
        int pink = 0;
        pink = 10;

        red = pink - 10;
    }

    public static void main(String[] args) {
        // Count Args
        int argsLength = args.length;
        Runnable sayArgsLength = new Runnable() {
            @Override
            public void run() {
                System.out.println("Length: " + argsLength);
            }
        };

        int pink = 0;
        pink = pink + 10;
        String[] cities = new String[]{"London", "Paris", "New York", "San Francisco"};
        cities[0] = "Berlin";
        System.out.println(cities[0]);

        HashMap<String, String> map = new HashMap<>();
        HashMap<String, String> map2 = new HashMap<>();

        // put a key in the map
       for (int i = 0; i < 10; i++) {
            System.out.println("i: " + cities[i]);
        }

        sayArgsLength.run();
        // Names
        String ian = "Ian";
        // ian

        // TextField
        JTextPane textName = new JTextPane();
        textName.setPreferredSize(new Dimension(300, 50));
        JTextPane textPassword = new JTextPane();
        textPassword.setPreferredSize(new Dimension(300, 50));
        JTextPane textEmail = new JTextPane();
        textEmail.setPreferredSize(new Dimension(300, 50));
        JTextPane textPhone = new JTextPane();
        textPhone.setPreferredSize(new Dimension(300, 50));
        JTextPane textAddress = new JTextPane();
        textAddress.setPreferredSize(new Dimension(300, 50));
        JTextPane textCity = new JTextPane();
        JButton buttonSubmit = new JButton("Submit");

        ActionListener printAllTextValues = (event) -> {
            System.out.println("Name: " + textName.getText());
            System.out.println("Password: " + textPassword.getText());
            System.out.println("Email: " + textEmail.getText());
            System.out.println("Phone: " + textPhone.getText());
            System.out.println("Address: " + textAddress.getText());
            System.out.println("City: " + textCity.getText());
        };

       buttonSubmit.addActionListener(printAllTextValues);
        String stringTest = "bing".toUpperCase(Locale.ENGLISH);
        stringTest = stringTest + "o";
        textName.setText(ian.toLowerCase(Locale.ROOT));
        textName.setPreferredSize(new Dimension(300, 50));
        textPassword.setText(stringTest);

        String password = "textpassword";
        String timeOfDay = "value";

       /*
        JTextPane textName {
            name: ian.toLowercase(Locale.ROOT)
            preferredSize: Dimension(200, 30)
            backgroundColor: Color(255, 255, 255)
        }
        */

        ActionListener submitAction = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Integer error = 0;
                System.out.println("Name: " + textName.getText());
                System.out.println("Password: " + textPassword.getText());
                System.out.println("Error: " + error);
            }
        };

        buttonSubmit.addActionListener(submitAction);

        textCity.setMinimumSize(new Dimension(400, 50));

        // create a list of strings
        for (int i = 0; i < 10; i++)
        {
            System.out.println("i: " + cities[i]);
            System.out.println(stringTest);
        }

        if(stringTest.equals("bing"))
        {
            System.out.println("bong");
            pink = pink + 10;
            pink = pink + 10;
        }
        else {
            System.out.println("not bing");
        }

    }

    public static double getTotalFloorNeeded()
    {
        double width = 10;
        double length = 10;

        return width * length;
    }

    public static String getWindowSearch() {
        return windowSearch;
    }

    public static void setWindowSearch(String windowSearch) {
        JavaPsi.windowSearch = windowSearch;
    }

}