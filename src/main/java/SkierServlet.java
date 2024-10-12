import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.json.*;



@WebServlet (value = "/skiers/*")
public class SkierServlet extends HttpServlet {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();


        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing paramterers");
            return;
        }

        String[] urlParts = urlPath.split("/");


        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            res.setStatus(HttpServletResponse.SC_OK);

            // TODO: process url params in `urlParts`
            String resortID = urlParts[1];
            String seasonID = urlParts[3];
            String dayID = urlParts[5];
            String skierID = urlParts[7];
            String responseMessage = String.format("ResortId: %s, SeasonId: %s, DayId: %s, SkierId: %s", resortID, seasonID, dayID, skierID);
            res.getWriter().write(responseMessage);
//            res.getWriter().write("It works!");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");


        String urlPath = req.getPathInfo();
        String[] urlParts = urlPath.split("/");
        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }


        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
        } catch (IOException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("{\"message\":\"Invalid request body\"}");
            return;
        }


        LiftRide liftRide;
        try {
            liftRide = parseLiftRide(jsonBuilder.toString());
        } catch (IllegalArgumentException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("{\"message\":\"" + e.getMessage() + "\"}");
            return;
        }


        res.setStatus(HttpServletResponse.SC_CREATED);
        res.getWriter().write("{\"message\":\"LiftRide recorded successfully\", \"liftID\":" + liftRide.getLiftID() + "}");
    }

    private LiftRide parseLiftRide(String json) {
        JSONObject jsonObject = new JSONObject(json);

        if (!jsonObject.has("time") || !jsonObject.has("liftID")) {
            throw new IllegalArgumentException("Missing required fields: time and liftID");
        }

        int time = jsonObject.getInt("time");
        int liftID = jsonObject.getInt("liftID");


        LiftRide liftRide = new LiftRide();
        liftRide.setTime(time);
        liftRide.setLiftID(liftID);
        return liftRide;
    }


    private static class LiftRide {
        private Integer time;
        private Integer liftID;

        public Integer getTime() {
            return time;
        }

        public void setTime(Integer time) {
            this.time = time;
        }

        public Integer getLiftID() {
            return liftID;
        }

        public void setLiftID(Integer liftID) {
            this.liftID = liftID;
        }
    }



    private boolean isUrlValid(String[] urlPath) {
        // TODO: validate the request url path according to the API spec
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        if (urlPath.length != 8) {
            return false;
        }

        if (!urlPath[2].equals("seasons") || !urlPath[4].equals("day") || !urlPath[6].equals("skier")) {
            return false;
        }

        try {
            Integer.parseInt(urlPath[1]); // resortID
            Integer.parseInt(urlPath[3]); // seasonID
            int dayID = Integer.parseInt(urlPath[5]); // dayID
            Integer.parseInt(urlPath[7]); // skierID


            if (dayID < 1 || dayID > 366) {
                return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

}
