package com.sundayschool.persistence;

public class WinnersInfo {
    private String firstName;
    private String middleName;
    private String lastName;
    private String church;
    private String ssGroup;
    private int totalMarks;

    
    
    public WinnersInfo(String firstName, String middleName, String lastName, String church, String ssGroup,
			int totalMarks) {
		super();
		this.firstName = firstName;
		this.middleName = middleName;
		this.lastName = lastName;
		this.church = church;
		this.ssGroup = ssGroup;
		this.totalMarks = totalMarks;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getChurch() {
        return church;
    }

    public void setChurch(String church) {
        this.church = church;
    }

    public String getSsGroup() {
        return ssGroup;
    }

    public void setSsGroup(String ssGroup) {
        this.ssGroup = ssGroup;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }
}
