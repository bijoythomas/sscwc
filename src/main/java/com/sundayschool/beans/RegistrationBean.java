package com.sundayschool.beans;

import com.sundayschool.constants.Groups;
import com.sundayschool.persistence.HibernateUtil;
import com.sundayschool.persistence.RegionToChurch;
import com.sundayschool.persistence.StudentInfo;
import com.sundayschool.persistence.UserToRegion;
import com.sundayschool.persistence.WinnersInfo;

import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.primefaces.model.chart.PieChartModel;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.security.Principal;
import java.util.*;

import static com.sundayschool.constants.Categories.*;
import static com.sundayschool.constants.Groups.*;
import static com.sundayschool.persistence.StudentInfo.categoryMapLookup;

@ManagedBean
@SessionScoped
public class RegistrationBean {
    protected List<StudentInfo> studentInfoList = new ArrayList<StudentInfo>();
    private List<DistributionData> distributionDataList = new LinkedList<DistributionData>();
    protected List<StudentInfo> searchList = new ArrayList<StudentInfo>();
    protected List<WinnersInfo> winnersList = new ArrayList<WinnersInfo>();
    protected String id;
    protected String firstName;
    protected String middleName;
    protected String lastName;
    protected String category;
    protected String group;
    protected String church;
    protected String venue;
    List<String> availableChurches;
    List<String> venueChurches;
    List<String> availableCategories;
    List<String> availableGroups;
    List<String> winnersGroups;
    String winnersGroups4to7 = "Groups 4 to 7";
    String winnersGroups2to3 = "Groups 2 and 3";
    protected String winnersGroup;

    private PieChartModel pieModel = new PieChartModel();

    public String remove(StudentInfo studentInfo) {
        studentInfoList.remove(studentInfo);
        return null;
    }

    public String removeSearchItem(StudentInfo studentInfo) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("delete StudentInfo where church = :church and ssGroup = :ssGroup and categoryCode = :categoryCode and firstName = :firstName and lastName = :lastName");
        query.setParameter("church", studentInfo.getChurch());
        query.setParameter("ssGroup", studentInfo.getSsGroup());
        query.setParameter("categoryCode", studentInfo.getCategoryCode());
        query.setParameter("firstName", studentInfo.getFirstName());
        query.setParameter("lastName", studentInfo.getLastName());
        int result = query.executeUpdate();
        transaction.commit();
        session.close();
        searchList.remove(studentInfo);
        return null;
    }

    public String updateRegistration() {
        String firstName = (this.firstName == null || this.firstName.trim().equals("")) ? "" : this.firstName;
        String middleName = (this.middleName == null || this.middleName.trim().equals("")) ? "" : this.middleName;
        String lastName = (this.lastName == null || this.lastName.trim().equals("")) ? "" : this.lastName;

        if (firstName.isEmpty() || lastName.isEmpty()) {
            FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error message", "You must provide both first name and last name");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            return null;
        }

        if (id == null || id.isEmpty()) {
            FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error message", "You must provide both a registration ID");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            return null;
        }

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        Query query = session.createQuery("update StudentInfo set firstName = :firstName, middleName = :middleName, lastName = :lastName, ssGroup = :ssGroup, church = :church, categoryCode = :categoryCode, venue = :venue where id = :id");
        query.setParameter("church", church);
        query.setParameter("ssGroup", group);
        query.setParameter("categoryCode", categoryMapLookup.get(this.category));
        query.setParameter("venue", venue);
        query.setParameter("firstName", firstName);
        query.setParameter("middleName", middleName);
        query.setParameter("lastName", lastName);
        query.setParameter("id", id);
        int result = query.executeUpdate();
        transaction.commit();
        session.close();

        if (result == 1) {
            FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, "Info message", "Information for ID " + id + " is updated");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            return null;
        } else {
            FacesMessage facesMessage = new FacesMessage("Error updating registration information");
            FacesContext.getCurrentInstance().addMessage(null, facesMessage);
            return null;
        }
    }

    public String findWinners() {
    	winnersList.clear();
    	List<String> groupsToQuery, categoriesToQuery;
    	Map<String, WinnersInfo> winnersMap = new HashMap<String, WinnersInfo>();
    	String region = getRegionForUser();
    	if (this.winnersGroup.equals(winnersGroups2to3)) {
    		groupsToQuery = Arrays.asList(GROUP_2, GROUP_3);
    		categoriesToQuery = Arrays.asList(BIBLE_QUIZ, DRAWING); // Only bible quiz and drawing
    	} else {
    		groupsToQuery = Arrays.asList(GROUP_4, GROUP_5, GROUP_6, GROUP_7);
    		categoriesToQuery = getAvailableCategories(); // All categories
    	}
    	
    	for (String ssGroup: groupsToQuery) {
    		for (String category: categoriesToQuery) {
    			final String ssCategory = category;
    			int highest = 3, currentScore=Integer.MIN_VALUE;
    			List<StudentInfo> winnersInGroupAndCategory = new LinkedList<StudentInfo>();
    			String dbCategory = categoryMapLookup.get(category);
    			Session session = HibernateUtil.getSessionFactory().openSession();
    			Query query = session.createQuery("from StudentInfo where region = :region and ssGroup like :ssGroup and categoryCode like :dbCategory");
    			query.setParameter("region", region);
    	        query.setParameter("ssGroup", ssGroup);
    	        query.setParameter("dbCategory", dbCategory);
    	        List<StudentInfo> studentsinGroup = query.list();
    	        session.close();
    	        Collections.sort(studentsinGroup, new Comparator<StudentInfo>() {
    	        	public int compare(StudentInfo a, StudentInfo b) {
    	        		//return b.getJudge1Marks() - a.getJudge1Marks();
    	        		if (ssCategory.equals(BIBLE_QUIZ)) {
    	        			return b.getJudge1Marks() - a.getJudge1Marks();
    	        		} else {
//    	        			return b.getTotalMarks() - a.getTotalMarks();
    	        			return b.getJudge1Marks() - a.getJudge1Marks();
    	        		}
    	        	}
				});
    	        for(StudentInfo student: studentsinGroup) {
//    	        	int score = (ssCategory.equals(BIBLE_QUIZ))? student.getJudge1Marks(): student.getTotalMarks();
    	        	int score = student.getJudge1Marks();
    	        	if (score != currentScore) {
    	        		currentScore = score;
    	        		highest--;
    	        	}
    	        	
    	        	if (highest < 0) {
    	        		break;
    	        	}
    	        	
    	        	String firstName = student.getFirstName().trim(), lastName = student.getLastName().trim(), church = student.getChurch(), group = student.getSsGroup();
    	        	String middleName = (student.getMiddleName() == null? "" : student.getMiddleName().trim());
    	        	String key = firstName + " " + middleName + " " + lastName + " " + group + " " + church;
    	        	WinnersInfo winner = winnersMap.get(key);
    	        	if (winner == null) {
    	        		winner = new WinnersInfo(firstName, middleName, lastName, church, group, 0);
    	        		winnersMap.put(key, winner);
    	        	}
    	        	winner.setTotalMarks(winner.getTotalMarks() + highest + 1);
    	        	
    	        }
    	        
    		}
    	}
    	this.winnersList.addAll(winnersMap.values());
//    	WinnersInfo winner = new WinnersInfo();
//    	winner.setFirstName("Bijoy");
//    	winner.setMiddleName("Jacob");
//    	winner.setLastName("Thomas");
//    	winner.setChurch("St. Marys FB");
//    	winner.setSsGroup("Group 4");
//		winner.setTotalMarks(12);
//    	winnersList.add(winner);
    	return null;	
    }
      
    public List<WinnersInfo> getWinnersList() {
		return winnersList;
	}

	public void setWinnersList(List<WinnersInfo> winnersList) {
		this.winnersList = winnersList;
	}

	public String performSearch() {
        searchList.clear();
        Session session = HibernateUtil.getSessionFactory().openSession();
        String church = this.church.equals("All") ? "%" : this.church;
        String group = this.group.equals("All") ? "%" : this.group;
        String venue = this.venue.equals("All") ? "%" : this.venue;
        String category = this.category.equals("All") ? "%" : categoryMapLookup.get(this.category);
        String firstName = (this.firstName == null || this.firstName.trim().equals("")) ? "%" : this.firstName;
        String middleName = (this.middleName == null || this.middleName.trim().equals("")) ? "%" : this.middleName;
        String lastName = (this.lastName == null || this.lastName.trim().equals("")) ? "%" : this.lastName;
        String region = getRegionForUser();
        String firstNameMatch = "firstName like :firstName";
        if (firstName.equals("%")) {
            firstNameMatch = "(" + firstNameMatch + " or " + "firstName is NULL" + ")";
        }
        String middleNameMatch = "middleName like :middleName";
        if (middleName.equals("%")) {
            middleNameMatch = "(" + middleNameMatch + " or " + "middleName is NULL" + ")";
        }

        String lastNameMatch = "lastName like :lastName";
        if (lastName.equals("%")) {
            lastNameMatch = "(" + lastNameMatch + " or " + "lastName is NULL" + ")";
        }
        Query query = session.createQuery("from StudentInfo where region = :region and church like :church and ssGroup like :ssGroup and categoryCode like :categoryCode and venue like :venue and " + firstNameMatch + " and " + middleNameMatch + " and " + lastNameMatch);
        query.setParameter("region", region);
        query.setParameter("church", church);
        query.setParameter("ssGroup", group);
        query.setParameter("categoryCode", category);
        query.setParameter("venue", venue);
        query.setParameter("firstName", firstName);
        query.setParameter("middleName", middleName);
        query.setParameter("lastName", lastName);
        searchList.addAll(query.list());
        session.close();
        return null;
    }

    private String getUser() {
        final Principal principal = FacesContext.getCurrentInstance()
                .getExternalContext().getUserPrincipal();
        if (null == principal) {
            return null;
        }
        return principal.getName();
    }


    public String addAction() {
        String region = getRegionForUser();
        StudentInfo studentInfo = new StudentInfo(this.firstName, this.middleName, this.lastName, region, this.church, this.category, this.group, this.venue);
        studentInfoList.add(studentInfo);
        return null;
    }

    private String getRegionForUser() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        String loggedInUser = getUser();
        Query query = session.createQuery("from UserToRegion where username like :username");
        query.setParameter("username", loggedInUser);
        List<UserToRegion> list = query.list();
        session.close();
        return list.get(0).getRegion();
    }

    public String confirm() {
        try {
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = session.beginTransaction();
            for (StudentInfo studentInfo : studentInfoList) {
                session.save(studentInfo);
            }
            transaction.commit();
            session.flush();
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        studentInfoList.clear();
        FacesMessage facesMessage = new FacesMessage("Successfully Added the following users");
        FacesContext.getCurrentInstance().addMessage(null, facesMessage);
        return null;
    }

    public void displayData() {
        pieModel.clear();
        distributionDataList.clear();
        Session session = HibernateUtil.getSessionFactory().openSession();
        Query query = session.createQuery("from StudentInfo where ssGroup like :ssGroup and categoryCode like :categoryCode");
        String group = this.group.equals("All") ? "%" : this.group;
        query.setParameter("ssGroup", group);
        String category = this.category.equals("All") ? "%" : categoryMapLookup.get(this.category);
        query.setParameter("categoryCode", category);
        List<StudentInfo> list = query.list();
        Map<String, Integer> results = new HashMap<String, Integer>();
        for (StudentInfo studentInfo : list) {
            Integer integer = results.get(studentInfo.getChurch());
            if (integer == null) {
                results.put(studentInfo.getChurch(), 1);
            } else {
                results.put(studentInfo.getChurch(), integer + 1);
            }
        }

        for (Map.Entry<String, Integer> resultsEntry : results.entrySet()) {
            pieModel.set(resultsEntry.getKey(), resultsEntry.getValue());
            distributionDataList.add(new DistributionData(resultsEntry.getKey(), resultsEntry.getValue().toString()));
        }
        session.close();
    }

    public List<DistributionData> getDistributionDataList() {
        return distributionDataList;
    }

    public void setDistributionDataList(List<DistributionData> distributionDataList) {
        this.distributionDataList = distributionDataList;
    }

    public PieChartModel getPieModel() {
        if (pieModel.getData().isEmpty()) {
            pieModel.set("No Data", 100);
        }
        return pieModel;
    }

    public void setPieModel(PieChartModel pieModel) {
        this.pieModel = pieModel;
    }

    public List getStudentInfoList() {
        return studentInfoList;
    }

    public void setStudentInfoList(List studentInfoList) {
        this.studentInfoList = studentInfoList;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getWinnersGroup() {
        return winnersGroup;
    }

    public void setWinnersGroup(String group) {
        this.winnersGroup = group;
    }
    
    public String getChurch() {
        return church;
    }

    public void setChurch(String church) {
        this.church = church;
    }

    public List<StudentInfo> getSearchList() {
        return searchList;
    }

    public void setSearchList(List<StudentInfo> searchList) {
        this.searchList = searchList;
    }

    public List<String> getAvailableChurches() {
        String regionForUser = getRegionForUser();
        Session session = HibernateUtil.getSessionFactory().openSession();
        Query query = session.createQuery("from RegionToChurch where region like :region");
        query.setParameter("region", regionForUser);
        List<RegionToChurch> list = query.list();
        LinkedList<String> listToReturn = new LinkedList<String>();
        for (RegionToChurch regionToChurch : list) {
            listToReturn.add(regionToChurch.getChurch());
        }

        return listToReturn;

//        return Arrays.asList(ChurchNames.SMFB, ChurchNames.SGIR, ChurchNames.SMCOI,
//                ChurchNames.STDL, ChurchNames.SPPL, ChurchNames.SGGL, ChurchNames.STJM, ChurchNames.STOK, ChurchNames.STCL);
    }

    public void setAvailableChurches(List<String> availableChurches) {
        this.availableChurches = availableChurches;
    }

    public List<String> getVenueChurches() {
        String regionForUser = getRegionForUser();
        Session session = HibernateUtil.getSessionFactory().openSession();
        Query query = session.createQuery("from RegionToChurch where region like :region");
        query.setParameter("region", regionForUser);
        List<RegionToChurch> list = query.list();
        LinkedList<String> listToReturn = new LinkedList<String>();
        for (RegionToChurch regionToChurch : list) {
            if (regionToChurch.getVenue()) {
                listToReturn.add(regionToChurch.getChurch());
            }
        }

        return listToReturn;
    }

    public void setVenueChurches(List<String> venueChurches) {
        this.venueChurches = venueChurches;
    }

    public List<String> getAvailableCategories() {
        return Arrays.asList(BIBLE_QUIZ, DRAWING, ESSAY_WRITING, STORY_WRITING, POETRY);
//        return Arrays.asList(GROUP_SONG_ENGLISH, GROUP_SONG_MALAYALAM, SOLO_SONG_ENGLISH, SOLO_SONG_MALAYALAM, ELOCUTION);
    }

    public void setAvailableCategories(List<String> availableCategories) {
        this.availableCategories = availableCategories;
    }

    public List<String> getAvailableGroups() {
        return Arrays.asList(GROUP_1, GROUP_2, GROUP_3, GROUP_4, GROUP_5, GROUP_6, GROUP_7);
    }

    public void setAvailableGroups(List<String> availableGroups) {
        this.availableGroups = availableGroups;
    }

    public List<String> getWinnersGroups() {
        return Arrays.asList(winnersGroups2to3, winnersGroups4to7);
    }

    public void setWinnersGroups(List<String> winnersGroups) {
        this.winnersGroups = winnersGroups;
    }
    
    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}