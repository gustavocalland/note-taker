package ca.finalproject.notetaker;

import android.util.Log;

/* Used to convert latitude/longitude data into EXIF format
 * Based on this answer from stackOverflow:
 *      http://stackoverflow.com/questions/5280479/how-to-save-gps-coordinates-in-exif-data-on-android
 * Decoder baded on this :
 *      http://www.blog.nathanhaze.com/how-to-get-exif-tags-gps-coordinates-time-date-from-a-picture/
 * @author fabien
*/
public class GPSDecoder {

    private static StringBuilder sb = new StringBuilder(20);

    /**
     * returns ref for latitude which is S or N.
     */
    public static String latitudeRef(double latitude) {
        return latitude<0.0d?"S":"N";
    }

    /**
     * returns ref for latitude which is S or N.
     */
    public static String longitudeRef(double longitude) {
        return longitude<0.0d?"W":"E";
    }

    /**
     * convert latitude into DMS (degree minute second) format. For instance<br/>
     * -79.948862 becomes<br/>
     *  79/1,56/1,55903/1000<br/>
     * It works for latitude and longitude<br/>
     * @param latitude could be longitude.
     * @return
     */
    synchronized public static final String convert(double latitude) {
        latitude=Math.abs(latitude);
        int degree = (int) latitude;
        latitude *= 60;
        latitude -= (degree * 60.0d);
        int minute = (int) latitude;
        latitude *= 60;
        latitude -= (minute * 60.0d);
        int second = (int) (latitude*1000.0d);

        sb.setLength(0);
        sb.append(degree);
        sb.append("/1,");
        sb.append(minute);
        sb.append("/1,");
        sb.append(second);
        sb.append("/1000,");
        return sb.toString();
    }

    public static Float decode(String tagGPSLatLong, String tagGPSLatLongRef){
        Float result;
        String[] DMS = tagGPSLatLong.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0/S1;

        result = new Float(FloatD + (FloatM/60) + (FloatS/3600));
        if(tagGPSLatLongRef.equals("S") || tagGPSLatLongRef.equals("W")){
            result *= -1;
        }

        return result;

    };

}
