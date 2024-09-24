package entities;

//imports used for the books controller
import entities.util.JsfUtil;
import entities.util.PaginationHelper;
import jakarta.annotation.PostConstruct;
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
import java.util.List;
import java.util.Map;

//defining a name to reference the controller in the xhtml files
@Named(value = "booksController")
//adding view scoped to display the information
@ViewScoped
public class BooksController implements Serializable {

    //the attributes of the controller class
    @Resource
    private UserTransaction utx = null;
    //reference to the persistence file
    @PersistenceUnit(unitName = "COURSEWORK4PU")
    private EntityManagerFactory emf = null;

    private Books current;
    private DataModel items = null;
    private BooksJpaController jpaController = null;
    private PaginationHelper pagination;
    private int selectedItemIndex;
    private String selectedBookName;
    private String bookDescription;

    public BooksController() {
    }

    @PostConstruct
    public void init() {
        Map<String, String> requestParameterMap = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        selectedBookName = requestParameterMap.get("selectedBookName");
    }

    //getters and setters
    public List<Books> getAllBooks() {
        return getJpaController().findBooksEntities(); // Assuming this returns all entities
    }

    public String getSelectedBookName() {
        return selectedBookName;
    }

    public void setSelectedBookName(String selectedBookName) {
        this.selectedBookName = selectedBookName;
        fetchBookDescription();
    }

    public Books getNextBook() {
        List<Books> allBooks = getAllBooks();  // This method should return all books
        for (int i = 0; i < allBooks.size(); i++) {
            if (allBooks.get(i).getBookname().equals(selectedBookName) && i < allBooks.size() - 1) {
                return allBooks.get(i + 1);  // Returns the next book
            }
        }
        return null; // or return allBooks.get(0); for cyclic navigation
    }

    public Books getPreviousBook() {
        List<Books> allBooks = getAllBooks();  // This method should return all books
        for (int i = 0; i < allBooks.size(); i++) {
            if (allBooks.get(i).getBookname().equals(selectedBookName) && i > 0) {
                return allBooks.get(i - 1);  // Returns the previous book
            }
        }
        return null; // or return allBooks.get(allBooks.size() - 1); for cyclic navigation
    }

    public void selectNextBook() {
        Books nextBook = getNextBook();
        if (nextBook != null) {
            this.selectedBookName = nextBook.getBookname();
            fetchBookDescription(); // Fetch the description for the newly selected book
        }
    }

    public void selectPreviousBook() {
        Books previousBook = getPreviousBook();
        if (previousBook != null) {
            this.selectedBookName = previousBook.getBookname();
            fetchBookDescription(); // Fetch the description for the newly selected book
        }
    }

    public String getBookDescription() {
        return getJpaController().findBookDescriptionByName(selectedBookName);
    }

    //finding the book description 
    public void fetchBookDescription() {
        if (selectedBookName == null || selectedBookName.trim().isEmpty()) {
            bookDescription = "No book selected.";
            return;
        }

        try {
            Books book = getJpaController().findBooksByName(selectedBookName.trim());
            if (book != null) {
                bookDescription = book.getBookdescription(); // Assuming getDescription() returns the description
            } else {
                bookDescription = "Description not found for " + selectedBookName;
            }
        } catch (Exception e) {
            // Log the exception as per your logging framework
            bookDescription = "Error fetching description for " + selectedBookName;
        }
    }

    //function used to redirect the user to another page
    public void redirectToBookPage() {
        if (selectedBookName != null && !selectedBookName.trim().isEmpty()) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            String path = "/book.xhtml?faces-redirect=true&selectedBookName=" + selectedBookName.trim();
            facesContext.getApplication().getNavigationHandler().handleNavigation(facesContext, null, path);
        }
    }
    //more getters
    public Books getSelected() {
        if (current == null) {
            current = new Books();
            selectedItemIndex = -1;
        }
        return current;
    }

    public String getImagePath() {
        Books book = getJpaController().findBooksByName(selectedBookName.trim());
        if (book != null && book.getBookurl() != null) {
            return "/resources/images/" + book.getBookurl();
        }
        return "/resources/images/default.jpg"; // A default image if none is specified
    }

    private BooksJpaController getJpaController() {
        if (jpaController == null) {
            jpaController = new BooksJpaController(utx, emf);
        }
        return jpaController;
    }
    //function used to set the amount of data displayed in the crud operations table at a time. In this case 10 items will be displayed on each page
    public PaginationHelper getPagination() {
        if (pagination == null) {
            pagination = new PaginationHelper(10) {

                @Override
                public int getItemsCount() {
                    return getJpaController().getBooksCount();
                }

                @Override
                public DataModel createPageDataModel() {
                    return new ListDataModel(getJpaController().findBooksEntities(getPageSize(), getPageFirstItem()));
                }
            };
        }
        return pagination;
    }

    //preparing the pages for the crud operations
    public String prepareList() {
        recreateModel();
        return "List";
    }

    public String prepareView() {
        current = (Books) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "View";
    }

    public String prepareCreate() {
        current = new Books();
        selectedItemIndex = -1;
        return "Create";
    }

    public String create() {
        try {
            getJpaController().create(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("BooksCreated"));
            return prepareCreate();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    public String prepareEdit() {
        current = (Books) getItems().getRowData();
        selectedItemIndex = pagination.getPageFirstItem() + getItems().getRowIndex();
        return "Edit";
    }

    //updating the pages if any changes have been made
    public String update() {
        try {
            getJpaController().edit(current);
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("BooksUpdated"));
            return prepareEdit();
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
            return null;
        }
    }

    //destroying the slected item
    public String destroy() {
        current = (Books) getItems().getRowData();
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
            getJpaController().destroy(current.getBookname());
            JsfUtil.addSuccessMessage(ResourceBundle.getBundle("/Bundle").getString("BooksDeleted"));
        } catch (Exception e) {
            JsfUtil.addErrorMessage(e, ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
        }
    }

    //updating the current item
    private void updateCurrentItem() {
        int count = getJpaController().getBooksCount();
        if (selectedItemIndex >= count) {
            // selected index cannot be bigger than number of items:
            selectedItemIndex = count - 1;
            // go to previous page if last page disappeared:
            if (pagination.getPageFirstItem() >= count) {
                pagination.previousPage();
            }
        }
        if (selectedItemIndex >= 0) {
            current = getJpaController().findBooksEntities(1, selectedItemIndex).get(0);
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

    //function to load more data
    public String next() {
        getPagination().nextPage();
        recreateModel();
        return "List";
    }

    //function to retrieve the previous set of data
    public String previous() {
        getPagination().previousPage();
        recreateModel();
        return "List";
    }

    public SelectItem[] getItemsAvailableSelectMany() {
        return JsfUtil.getSelectItems(getJpaController().findBooksEntities(), false);
    }

    public SelectItem[] getItemsAvailableSelectOne() {
        return JsfUtil.getSelectItems(getJpaController().findBooksEntities(), true);
    }

    @FacesConverter(forClass = Books.class)
    public static class BooksControllerConverter implements Converter {

        @Override
        public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
            if (value == null || value.length() == 0) {
                return null;
            }
            BooksController controller = (BooksController) facesContext.getApplication().getELResolver().
                    getValue(facesContext.getELContext(), null, "booksController");
            return controller.getJpaController().findBooks(getKey(value));
        }

        java.lang.String getKey(String value) {
            java.lang.String key;
            key = value;
            return key;
        }

        String getStringKey(java.lang.String value) {
            StringBuilder sb = new StringBuilder();
            sb.append(value);
            return sb.toString();
        }

        @Override
        public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
            if (object == null) {
                return null;
            }
            if (object instanceof Books) {
                Books o = (Books) object;
                return getStringKey(o.getBookname());
            } else {
                throw new IllegalArgumentException("object " + object + " is of type " + object.getClass().getName() + "; expected type: " + Books.class.getName());
            }
        }

    }

}
