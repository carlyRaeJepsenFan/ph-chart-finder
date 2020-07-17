import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class popheadsParser {
   private static final LocalDate firstPooDate = LocalDate.parse("2017-11-06");
   private static final LocalDate lastPooDate = LocalDate.parse("2019-04-29");


   //prompts user for Song / Album, # of positions, and # of weeks ago
   public static void main(String[] args) {
      popheadsParser par = new popheadsParser();
      Scanner ask = new Scanner(System.in);
      System.out.println("Type S for songs or A for albums");
      String typeChoice = ask.nextLine().toUpperCase().trim();
      if(!(typeChoice.equals("S") || typeChoice.equals("A"))) {
         throw new IllegalArgumentException();
      }
      System.out.println("How many chart positions?");
      int chartChoice = ask.nextInt();
      System.out.println("How many weeks ago? (0 for this week)");
      int weekChoice = ask.nextInt();
      if(typeChoice.equals("S")) {
         par.getSongs(chartChoice, weekChoice);
      } else {
         par.getAlbums(chartChoice, weekChoice);
      }
   }
   
   public void getPopheads(int mode, int amount, int time) { //finds the top amount # of songs from time weeks ago
      LocalDate newDate = getDate("https://old.reddit.com/user/ImADudeDuh/submitted/");
      long long1 = ChronoUnit.WEEKS.between(lastPooDate, newDate);
      long long2 = ChronoUnit.WEEKS.between(firstPooDate, newDate);
      int weeksSincePoo = Math.toIntExact(long1);
      int weeksSinceFirst = Math.toIntExact(long2);
      if(amount < 1 || time < 0 || time > weeksSinceFirst - 50) {
         System.out.println("Please enter valid numbers.");
      } else if(time == weeksSincePoo + 1 || time == weeksSincePoo + 2)  {
         System.out.println("No chart for this week.");
      } else {
         if(mode == 1 && newDate.minusWeeks(time).getDayOfMonth() > 9) {
            getPopheads(1, amount, time + 1);
         }
         else if(time == weeksSincePoo - 35) {
              System.out.println("The Popheads Chart, December 30, 2019: Cruel Summer spending another week in the top 10 has got me going psycho, psycho");
              getChartData(2, amount, "https://pastebin.com/qxRYyjEL");
              if(amount > 50) {
                 System.out.println("Only 50 entries were found.");
              }
         } else if(time == weeksSincePoo - 16) {
              System.out.println("The Popheads Chart, August 19, 2019: Everyone at Popheads thinks you're the best thing since sliced bread");
              getChartData(2, amount, "https://pastebin.com/82yFHAmW");
         } else if(time >= weeksSincePoo) { //surfs u/letsallpoo's profile if chart is super old
              if(time > weeksSincePoo + 2) {
                 time -= 2;
              }
              getPopheads(mode, amount, time - weeksSincePoo, "https://old.reddit.com/user/letsallpoo/submitted/", 
                      weeksSincePoo, weeksSinceFirst);
         } else {
              if(time > weeksSincePoo - 14) {
                 time -= 2;
              } else if(time > weeksSincePoo - 33) {
                 time--;
              }
              getPopheads(mode, amount, time, "https://old.reddit.com/user/ImADudeDuh/submitted/", weeksSincePoo, weeksSinceFirst);
           }
        }
     }
   
   private LocalDate getDate(String dateURL) {
      try {
         Document dateCheck = Jsoup.connect(dateURL).get();
         try{
            String dateString = dateCheck.select("p:contains(The Popheads Chart)").first().text();
            return parseDate(dateString);
         } catch(Exception noPost){return getDate(dateCheck.select("[rel=nofollow next]").first().attr("href"));}
      } catch(Exception noPage){System.out.println("noPageException");}
      return null;
   }
   
   private boolean getChartData(int mode, int amount, String chartDataURL) {
      int lim = 52;
      if(mode == 2) {
         lim = 50;
      }
      try {
         Document pastebin = Jsoup.connect(chartDataURL).get();
         Elements chartBody = pastebin.select("#selectable");
         Elements pasteRows = chartBody.select("li");
         String keyMatch = "Rank|Song";
         if(mode == 1) {
            keyMatch = "Rank|Album";
         }
         if(pasteRows.size() > lim && pasteRows.get(0).text().startsWith(keyMatch, 0)) { //checks for correct link
            if(pasteRows.size() < amount + 2) {
               System.out.println("Only " + pasteRows.size() + " entries were found.");
               amount = pasteRows.size() - 2;
            }
            for(int j = 0; j < amount + 2; j++) {
               System.out.println(pasteRows.first().text());
               pasteRows = pasteRows.next();
            }
            return true;
         }
      } catch(Exception noPaste){System.out.println("noPasteException");}
      return false;
   }
   
   private void getPopheads(int mode, int amount, int time, String popURL, 
                            int weeksSincePoo, int weeksSinceFirst) { //helper method for finding earlier charts
      try{
         Document profile = Jsoup.connect(popURL).get();
         try{
            Elements links = profile.select("p:contains(The Popheads Chart)");
            if(links.size() < time + 1) { //goes to the next page if the chart is too old
               Element link = profile.select("[rel=nofollow next]").first();
               getPopheads(mode, amount, time - links.size(), link.attr("href"), weeksSincePoo, weeksSinceFirst);
            } else { //opens the post if conditions are met
               Element date = links.get(time);
               if(mode == 1) {
                  LocalDate oldDate = parseDate(date.text()).minusMonths(1);
                  System.out.println("Month of " + oldDate.getMonth() + " " + oldDate.getYear());
               } else {
                  System.out.println(date.text());
               }
               String chartLink = date.child(0).attr("href");
               String finalChartLink = "https://old.reddit.com" + chartLink;
               System.out.println("Link to Post: https://www.reddit.com" + chartLink);
               try {
                  Document chart = Jsoup.connect(finalChartLink).get();
                  Elements pastebins = chart.select("[href ^= https://pastebin.com]");
                  boolean found = false;
                  int loop = 0;
                  while(!found) { //cycles through all pastebin links on the page
                     String pasteString = pastebins.get(loop).attr("href");
                     found = getChartData(mode, amount, pasteString);
                     loop++;
                 }
               } catch(Exception noChart){System.out.println("noChartException");}
            }
          } catch(Exception noPost){getPopheads(mode, amount, time, profile.select("[rel=nofollow next]").first().attr("href"), 
                                                weeksSincePoo, weeksSinceFirst);}
      } catch(Exception noPage){System.out.println("noPageException");}
   }
   
   private LocalDate parseDate(String s) {
      Pattern p = Pattern.compile("(\\w+ \\w+. 20\\d\\d)");
      Matcher m = p.matcher(s);
      if(m.find()) {
         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
         try {
            return LocalDate.parse(m.group(0).replaceAll("(?<=\\d)(st|nd|rd|th)", ""), formatter);
         } catch(Exception wrongFormat){formatter = DateTimeFormatter.ofPattern("MMMM d yyyy");
            return LocalDate.parse(m.group(0).replaceAll("(?<=\\d)(st|nd|rd|th)", ""), formatter);}
      } 
      System.out.println("date not found");
      return null;
   }
   
   public void getSongs(int amount, int time) {
      getPopheads(0, amount, time);
   }
      
   public void getSongs(int amount) { //uses this week by default if no time is specified
      getPopheads(0, amount, 0);
   }
      
   public void getSongs() { //uses this week and 10 songs by default if no time or amount is specified
      getPopheads(0, 10, 0);
   }
   
   public void getAlbums(int amount, int time) {
      getPopheads(1, amount, time);
   }
   
   public void getAlbums(int amount) {
      getPopheads(1, amount, 0);
   }
   
   public void getAlbums() {
      getPopheads(1, 10, 0);
   }
}