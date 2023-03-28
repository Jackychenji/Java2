import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * This is just a demo for you, please run it on JDK17 (some statements may be not allowed in lower version).
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
                        Double.parseDouble(info[21]), Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1 finished
    public Map<String, Integer> getPtcpCountByInst() {
        Map<String, Integer> ptcpCountByInst = new HashMap<>();
        for (Course course : courses) {
            ptcpCountByInst.merge(course.institution, course.participants, Integer::sum);
        }
        return ptcpCountByInst;
    }

    //2 finished
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> ptcpCountByInstAndSubject = new HashMap<>();
        for (Course course : courses) {
            String key = course.institution + "-" + course.subject;
            ptcpCountByInstAndSubject.merge(key, course.participants, Integer::sum);
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(ptcpCountByInstAndSubject.entrySet());

        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));

        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<Course>> coursesByInstructor = courses.stream()
                .flatMap(course -> Arrays.stream(course.instructors.split(",\\s*")))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toMap(
                        instructor -> instructor,
                        instructor -> courses.stream()
                                .filter(course -> course.instructors.contains(instructor))
                                .collect(Collectors.toList())
                ));
        Map<String, List<List<String>>> courseListByInstructor = new HashMap<>();
        for (Map.Entry<String, List<Course>> entry : coursesByInstructor.entrySet()) {
            String instructor = entry.getKey();
            List<Course> courses = entry.getValue();
            List<List<String>> courseList = courses.stream()
                    .map(course -> Arrays.asList(course.subject, course.title))
                    .collect(Collectors.toList());
            courseListByInstructor.put(instructor, courseList);
        }
        return courseListByInstructor;
    }

    //4 finished
    public List<String> getCourses(int topK, String by) {
        Comparator<Course> comparator = null;
        if (by.equals("hours")) {
            comparator = Comparator.comparingDouble(Course::getTotalHours).reversed()
                    .thenComparing(Course::getTitle);
        } else if (by.equals("participants")) {
            comparator = Comparator.comparingInt(Course::getParticipants).reversed()
                    .thenComparing(Course::getTitle);
        } else {
            throw new IllegalArgumentException("Invalid sorting criteria");
        }

        return courses.stream()
                .sorted(comparator)
                .map(Course::getTitle).distinct().limit(topK).toList();

    }


    //5 finished
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        return courses.stream()
                .filter(course -> course.subject.toLowerCase().contains(courseSubject.toLowerCase())
                        && course.percentAudited >= percentAudited
                        && course.totalHours <= totalCourseHours)
                .map(course -> course.title).distinct().sorted().collect(Collectors.toList());
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        List<Course> courses1 = new ArrayList<>();
        for (Course value : courses) {
            courses1.add(new Course(value));
        }
        HashMap<String,Integer> map3 = new HashMap<>();
        HashMap<String,Course> map = new HashMap<>();
        for (Course cours : courses1) {
            if (!map.containsKey(cours.number)) {
                map3.put(cours.number,1);
                map.put(cours.number, cours);
            }else {
                Course course = map.get(cours.number);
                if (course.launchDate.before(cours.launchDate)){
                    cours.percentDegree = (cours.percentDegree+course.percentDegree*map3.get(course.number))/(map3.get(course.number)+1);
                    cours.percentMale = (cours.percentMale+course.percentMale*map3.get(course.number))/(map3.get(course.number)+1);
                    cours.medianAge = (cours.medianAge+course.medianAge*map3.get(course.number))/(map3.get(course.number)+1);
                    map.replace(cours.number,course,cours);
                    map3.replace(cours.number,map3.get(cours.number),map3.get(cours.number)+1);
                }else{
                    course.percentDegree = (cours.percentDegree+course.percentDegree*map3.get(course.number))/(map3.get(course.number)+1);
                    course.percentMale = (cours.percentMale+course.percentMale*map3.get(course.number))/(map3.get(course.number)+1);
                    course.medianAge = (cours.medianAge+course.medianAge*map3.get(course.number))/(map3.get(course.number)+1);
                    map3.replace(course.number,map3.get(course.number),map3.get(course.number)+1);
                }
            }
        }
        List<Course> newCourses = new ArrayList<>(map.values());
        HashMap<String,Course> map2 = new HashMap<>();
        for (Course cours : newCourses) {
            if (!map2.containsKey(cours.title)) {
                map2.put(cours.title, cours);
            }else{
                Course course = map2.get(cours.title);
                double coursSum = Math.pow((age-cours.medianAge),2)+
                        Math.pow(gender*100-cours.percentMale,2)+Math.pow(isBachelorOrHigher*100- cours.percentDegree,2);
                double courseSum =  Math.pow((age-course.medianAge),2)+
                        Math.pow(gender*100-course.percentMale,2)+Math.pow(isBachelorOrHigher*100- course.percentDegree,2);
                if (coursSum<courseSum)map2.replace(cours.title,course,cours);
            }
        }
        newCourses = new ArrayList<>(map2.values());
        double[] Average = new double[newCourses.size()];
        for (int i = 0; i < Average.length; i++) {
            Average[i] = (age-newCourses.get(i).medianAge) * (age-newCourses.get(i).medianAge)+
                    (gender * 100 - newCourses.get(i).percentMale) * (gender * 100 - newCourses.get(i).percentMale)+
                    (isBachelorOrHigher * 100 - newCourses.get(i).percentDegree) * (isBachelorOrHigher * 100 - newCourses.get(i).percentDegree);
        }
        for (int i = 0; i < Average.length; i++) {
            for (int j = i; j < Average.length; j++) {
                if (Average[i]>Average[j]){
                    double temp = Average[i];
                    Average[i] = Average[j];
                    Average[j] = temp;
                    Course temp1 = newCourses.get(i);
                    newCourses.set(i,newCourses.get(j));
                    newCourses.set(j,temp1);
                }
            }
        }
        List<String> answer = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            answer.add(newCourses.get(i).title);
        }
        return answer;
//        return newCourses.stream().sorted(Comparator.comparingDouble(
//                        course -> (age-course.medianAge) * (age-course.medianAge) +
//                                (gender * 100 - course.percentMale) * (gender * 100 - course.percentMale)
//                                + (isBachelorOrHigher * 100 - course.percentDegree) * (isBachelorOrHigher * 100 - course.percentDegree)))
//                .map(course -> course.title).limit(10).toList();
    }

}

class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public String getInstitution() {
        return institution;
    }

    public String getNumber() {
        return number;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    public String getTitle() {
        return title;
    }

    public String getInstructors() {
        return instructors;
    }

    public String getSubject() {
        return subject;
    }

    public int getYear() {
        return year;
    }

    public int getHonorCode() {
        return honorCode;
    }

    public int getParticipants() {
        return participants;
    }

    public int getAudited() {
        return audited;
    }

    public int getCertified() {
        return certified;
    }

    public double getPercentAudited() {
        return percentAudited;
    }

    public double getPercentCertified() {
        return percentCertified;
    }

    public double getPercentCertified50() {
        return percentCertified50;
    }

    public double getPercentVideo() {
        return percentVideo;
    }

    public double getPercentForum() {
        return percentForum;
    }

    public double getGradeHigherZero() {
        return gradeHigherZero;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public double getMedianHoursCertification() {
        return medianHoursCertification;
    }

    public double getMedianAge() {
        return medianAge;
    }

    public double getPercentMale() {
        return percentMale;
    }

    public double getPercentFemale() {
        return percentFemale;
    }

    public double getPercentDegree() {
        return percentDegree;
    }

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }
    public Course(Course course){
        this.institution = course.institution;
        this.number = course.number;
        this.launchDate = course.launchDate;
        if (course.title.startsWith("\"")) course.title = course.title.substring(1);
        if (course.title.endsWith("\"")) course.title = course.title.substring(0, course.title.length() - 1);
        this.title = course.title;
        if (course.instructors.startsWith("\"")) course.instructors = course.instructors.substring(1);
        if (course.instructors.endsWith("\"")) course.instructors = course.instructors.substring(0, course.instructors.length() - 1);
        this.instructors = course.instructors;
        if (course.subject.startsWith("\"")) course.subject = course.subject.substring(1);
        if (course.subject.endsWith("\"")) course.subject = course.subject.substring(0, course.subject.length() - 1);
        this.subject = course.subject;
        this.year = course.year;
        this.honorCode = course.honorCode;
        this.participants = course.participants;
        this.audited = course.audited;
        this.certified = course.certified;
        this.percentAudited = course.percentAudited;
        this.percentCertified = course.percentCertified;
        this.percentCertified50 = course.percentCertified50;
        this.percentVideo = course.percentVideo;
        this.percentForum = course.percentForum;
        this.gradeHigherZero = course.gradeHigherZero;
        this.totalHours = course.totalHours;
        this.medianHoursCertification = course.medianHoursCertification;
        this.medianAge = course.medianAge;
        this.percentMale = course.percentMale;
        this.percentFemale = course.percentFemale;
        this.percentDegree = course.percentDegree;
    }
}