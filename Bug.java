import java.sql.Date;
public class Bug {
    private final Integer bugId;
    private final String title;
    private final String description;
    private final String artifactType;
    private final String priority;
    private final String status;
    private final String reportedBy;
    private final Date dateFound;
    private final Date dateFixed;

    public Bug(Integer bugId, String title, String description,
               String artifactType, String priority, String status,
               String reportedBy, Date dateFound, Date dateFixed) {
        this.bugId = bugId;
        this.title = title;
        this.description = description;
        this.artifactType = artifactType;
        this.priority = priority;
        this.status = status;
        this.reportedBy = reportedBy;
        this.dateFound = dateFound;
        this.dateFixed = dateFixed;
    }

    public Integer getBugId() { return bugId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getArtifactType() { return artifactType; }
    public String getPriority() { return priority; }
    public String getStatus() { return status; }
    public String getReportedBy() { return reportedBy; }
     public Date getDateFound() { return dateFound; }
    public Date getDateFixed() { return dateFixed; }
}