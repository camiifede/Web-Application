package entities;

//imports used for the books controller
import entities.util.JsfUtil;
import entities.util.PaginationHelper;
import java.io.Serializable;
import java.util.ResourceBundle;
import jakarta.annotation.Resource;
import jakarta.inject.Named;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.ListDataModel;
import jakarta.faces.model.SelectItem;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.transaction.UserTransaction;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.List;

//defining a name to reference the controller in the xhtml files
@Named("greekgodsController")
//adding view scoped to display the information
@ViewScoped
public class GreekgodsController implements Serializable {

    //the attributes of the controller class
    @Resource
    private UserTransaction utx = null;
    //reference to the persistence file
    @PersistenceUnit(unitName = "COURSEWORK4PU")
    private EntityManagerFactory emf = null;
    private String selectedGodName;
    private String godDescription;
    private Greekgods current;
    private DataModel items = null;
    private GreekgodsJpaController jpaController = null;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private String bookName;  // field to hold the book name.

    public GreekgodsController() {
    }

    @PostConstruct
    public void init() {
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedGodName = requestParameterMap.get("selectedGodName");
    }

    //getters and setters
    public List<Greekgods> getAllGods() {
        return getJpaController().findGreekgodsEntities(); // Assuming this returns all entities
    }

    public String getSelectedGodName() {
        return selectedGodName;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public void setSelectedGodName(String selectedGodName) {
        this.selectedGodName = selectedGodName;
        fetchGodDescription();
    }

    public String getGodDescription() {

        return getJpaController().findGodDescriptionByName(selectedGodName);
    }

    public void init(String godName) {
        this.selectedGodName = godName;
        fetchGodDescription();
    }
    //redirecting the user to the selected god page
    public void redirectToGodPage() {
        if (selectedGodName != null && !selectedGodName.trim().isEmpty()) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            String path = "/god.xhtml?faces-redirect=true&selectedGodName=" + selectedGodName.trim();
            facesContext.getApplication().getNavigationHandler().handleNavigation(facesContext, null, path);
        }
    }

    //selecting the next god in the list
    public Greekgods getNextGod() {
        List<Greekgods> allGods = getAllGods();
        for (int i = 0; i < allGods.size(); i++) {
            if (allGods.get(i).getGodname().equals(selectedGodName) && i < allGods.size() - 1) {
                return allGods.get(i + 1);
            }
        }
        return null; // or return first god for cyclic navigation
    }

    //selecting the previous god in the list
    public Greekgods getPreviousGod() {
        List<Greekgods> allGods = getAllGods();
        for (int i = 0; i < allGods.size(); i++) {
            if (allGods.get(i).getGodname().equals(selectedGodName) && i > 0) {
                return allGods.get(i - 1);
            }
        }
        return null; // or return last god for cyclic navigation
    }

    public void selectNextGod() {
        Greekgods nextGod = getNextGod();
        if (nextGod != null) {
            this.selectedGodName = nextGod.getGodname();
            fetchGodDescription(); // Assuming you have this method to update the description based on the selected god
        }
    }

    public void selectPreviousGod() {
        Greekgods previousGod = getPreviousGod();
        if (previousGod != null) {
            this.selectedGodName = previousGod.getGodname();
            fetchGodDescription();
        }
    }

    //retrieving the god description given the name
    public void fetchGodDescription() {
        if (selectedGodName == null || selectedGodName.trim().isEmpty()) {
            godDescription = "No god selected.";
            return;
        }

        try {
            Greekgods god = getJpaController().findGreekgodsByName(selectedGodName.trim());
            if (god != null) {
                godDescription = god.getGoddescription(); // Assuming getGoddescription() returns the description
            } else {
                godDescription = "An invalid god name has been entered:  " + selectedGodName;
            }
        } catch (Exception e) {
            // Log the exception as per your logging framework
            godDescription = "Error fetching description for " + selectedGodName;
        }
    }

    //retrieving the book name associated to the selected god
    public String findBookNameByGodName(String godName) {
        Greekgods god = getJpaController().findGreekgodsByName(godName);
        if (god != null && god.getBookname() != null) {
            return god.getBookname().getBookname();
        } else {
            return "No book associated with this god";
        }
    }

    public String findBookNameByGodName() {
        if (this.selectedGodName == null || this.selectedGodName.trim().isEmpty()) {
            this.bookName = "Please enter a valid god name.";
            return null;  // Stay on the same page, showing an error message.
        }

        Greekgods god = getJpaController().findGreekgodsByName(this.selectedGodName.trim());
        if (god != null && god.getBookname() != null) {
            this.bookName = god.getBookname().getBookname();
        } else {
            this.bookName = "No book associated with this god";
        }
        return "collections.xhtml?faces-redirect=true";  // Navigate to collections.xhtml
    }

    public Books getBookBySelectedGod() {
        if (selectedGodName != null && !selectedGodName.trim().isEmpty()) {
            Greekgods god = getJpaController().findGreekgodsByName(selectedGodName.trim());
            if (god != null) {
                return god.getBookname();  // Assuming 'getBookname' returns the associated book
            }
        }
        return null;  // Return null if no god is selected or the god does not have an associated book
    }

    //retrieving the image given the selected god
    public String getImagePath() {
        Greekgods god = getJpaController().findGreekgodsByName(selectedGodName.trim());
        if (god != null && god.getImageurl() != null) {
            return "/resources/" + god.getImageurl();
        }
        return "/resources/images/default.jpg"; // A default image if none is specified
    }

    public Greekgods getSelected() {
        if (current == null) {
            current = new Greekgods();
            selectedItemIndex = -1;
        }
        return current;
    }

    private GreekgodsJpaController getJpaController() {
        if (jpaController == null) {
            jpaController = new GreekgodsJpaController(utx, emf);
        }
        return jpaController;
    }

    //setting a pagination method to display the data in the database
    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(20) {

                @Override
                public int getItemsCount() {
                    return getJpaController().getGreekgodsCount();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getJpaController().findGreekgodsEntities(getPageSize(), getPageFirstItem()));
                }
            };
        }
        return pagination;
    }

    //creating a list method
    public String prepareList() {
        recreateModel();
        return "List";
    }

    //creating a view method
    public String prepareView() {
        current = (Greekgods) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    //creating a create item method
    public String prepareCreate() {
        current = new Greekgods();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getJpaController().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("GreekgodsCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    //creating an edit method
    public String prepareEdit() {
        current = (Greekgods) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    //creating an update method to refresh the page when new information is entered
    public String update() {
        try {
            getJpaController().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("GreekgodsUpdated"));
            return prepareEdit();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    //creating a method to delete an item
    public String destroy() {
        current = (Greekgods) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        performDestroy();
        recreatePagination();
        recreateModel();
        return "List";
    }

    public String destroyAndView() {
        performDestroy();
        recreateModel();
        updateCurrentItem();
        if (selectedItemIndex >= 0) {
            return "View";
        } else {
            // all items were removed - go back to list
            recreateModel();
            return "List";
        }
    }

    private void performDestroy() {
        try {
            getJpaController().destroy(current.getGodid());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("GreekgodsDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    private void updateCurrentItem() {
        int count = getJpaController().getGreekgodsCount();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getJpaController().findGreekgodsEntities(1, selectedItemIndex).get(0);
        }
    }

    public DataModel getItems() {
        if (items == null) {
            items = getPagination().createPageDataModel();
        }
        return items;
    }

    private void recreateModel() {
        items = null;
    }

    private void recreatePagination() {
        pagination = null;
    }

    //importing the next set of data to display to the user
    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    //importing the previous set of data to display to the user
    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(getJpaController().findGreekgodsEntities(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(getJpaController().findGreekgodsEntities(), true);
    }

    @FacesConverter(forClass = Greekgods.class)
    public static class GreekgodsControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            GreekgodsController controller = (GreekgodsController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "greekgodsController");
            return controller.getJpaController().findGreekgods(getKey(value));
        }

        java.lang.Integer getKey(String value) {
            java.lang.Integer key;
            key = Integer.valueOf(value);
            return key;
        }

        String getStringKey(java.lang.Integer value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Greekgods) {
                Greekgods o = (Greekgods) object;
                return getStringKey(o.getGodid());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Greekgods.class.getName());
            }
        }

    }

}
