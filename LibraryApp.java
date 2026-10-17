import java.util.*;

/* ===================== CUSTOM EXCEPTIONS ===================== */

class LibraryException extends Exception {
    public LibraryException(String message) {
        super(message);
    }
}

class BookNotAvailableException extends LibraryException {
    public BookNotAvailableException(String title) {
        super("Book '" + title + "' is already issued to someone else.");
    }
}

class BorrowLimitExceededException extends LibraryException {
    public BorrowLimitExceededException(String name, int limit) {
        super(name + " has reached the borrow limit of " + limit + " books.");
    }
}

/* ===================== INTERFACE ===================== */

interface Searchable {
    List<Book> search(String keyword);
}

/* ===================== BOOK ===================== */

class Book {
    private final int id;         
    private String title;
    private String author;
    private boolean available;

    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.available = true;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return String.format("[%d] %-28s by %-18s %s",
                id, title, author, available ? "(Available)" : "(Issued)");
    }

    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        return id == ((Book) o).id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

/* ===================== USER (ABSTRACT) ===================== */

abstract class User {
    private final int id;
    private String name;
    private final List<Book> issuedBooks = new ArrayList<>();

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }

    
    public List<Book> getIssuedBooks() {
        return Collections.unmodifiableList(issuedBooks);
    }

    void addBook(Book book) { issuedBooks.add(book); }
    void removeBook(Book book) { issuedBooks.remove(book); }

   
    public abstract int getBorrowLimit();
    public abstract String getRole();

    @Override
    public String toString() {
        return String.format("[%d] %-12s (%s, limit %d, currently holding %d)",
                id, name, getRole(), getBorrowLimit(), issuedBooks.size());
    }
}

/* ===================== SUBCLASSES (INHERITANCE) ===================== */

class Student extends User {
    public Student(int id, String name) {
        super(id, name);
    }

    @Override
    public int getBorrowLimit() { return 2; }

    @Override
    public String getRole() { return "Student"; }
}

class Teacher extends User {
    public Teacher(int id, String name) {
        super(id, name);
    }

    @Override
    public int getBorrowLimit() { return 5; }

    @Override
    public String getRole() { return "Teacher"; }
}

/* ===================== LIBRARY ===================== */

class Library implements Searchable {
    
    private final Map<Integer, Book> books = new LinkedHashMap<>();
    private final Map<Integer, User> users = new LinkedHashMap<>();
    private int nextBookId = 1;
    private int nextUserId = 1;

    public Book addBook(String title, String author) {
        Book book = new Book(nextBookId++, title, author);
        books.put(book.getId(), book);
        return book;
    }

    public void removeBook(int bookId) throws LibraryException {
        Book book = findBook(bookId);
        if (!book.isAvailable()) {
            throw new LibraryException("Cannot remove a book that is currently issued.");
        }
        books.remove(bookId);
    }

    public User registerUser(String name, boolean isTeacher) {
        
        User user = isTeacher
                ? new Teacher(nextUserId++, name)
                : new Student(nextUserId++, name);
        users.put(user.getId(), user);
        return user;
    }

    public void issueBook(int userId, int bookId) throws LibraryException {
        User user = findUser(userId);
        Book book = findBook(bookId);

        if (!book.isAvailable()) {
            throw new BookNotAvailableException(book.getTitle());
        }
        
        if (user.getIssuedBooks().size() >= user.getBorrowLimit()) {
            throw new BorrowLimitExceededException(user.getName(), user.getBorrowLimit());
        }

        book.setAvailable(false);
        user.addBook(book);
    }

    public void returnBook(int userId, int bookId) throws LibraryException {
        User user = findUser(userId);
        Book book = findBook(bookId);

        if (!user.getIssuedBooks().contains(book)) {   
            throw new LibraryException(user.getName() + " has not issued this book.");
        }

        book.setAvailable(true);
        user.removeBook(book);
    }

    @Override
    public List<Book> search(String keyword) {
        List<Book> result = new ArrayList<>();
        String key = keyword.toLowerCase();
        for (Book b : books.values()) {
            if (b.getTitle().toLowerCase().contains(key)
                    || b.getAuthor().toLowerCase().contains(key)) {
                result.add(b);
            }
        }
        return result;
    }

    public Collection<Book> getAllBooks() { return books.values(); }
    public Collection<User> getAllUsers() { return users.values(); }

    public List<Book> getIssuedBooks() {
        List<Book> issued = new ArrayList<>();
        for (Book b : books.values()) {
            if (!b.isAvailable()) issued.add(b);
        }
        return issued;
    }

    private Book findBook(int id) throws LibraryException {
        Book book = books.get(id);
        if (book == null) throw new LibraryException("No book found with id " + id);
        return book;
    }

    private User findUser(int id) throws LibraryException {
        User user = users.get(id);
        if (user == null) throw new LibraryException("No member found with id " + id);
        return user;
    }
}

/* ===================== MAIN (CONSOLE MENU) ===================== */

public class LibraryApp {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        Library library = new Library();

        
        library.addBook("Clean Code", "Robert Martin");
        library.addBook("Effective Java", "Joshua Bloch");
        library.addBook("Head First Java", "Kathy Sierra");
        library.addBook("Java: The Complete Reference", "Herbert Schildt");
        library.registerUser("Aman", false);   
        library.registerUser("Ms. Sharma", true); 

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Enter choice: ");
            try {
                switch (choice) {
                    case 1:
                        String title = readLine("Title: ");
                        String author = readLine("Author: ");
                        Book b = library.addBook(title, author);
                        System.out.println("Added: " + b);
                        break;
                    case 2:
                        library.removeBook(readInt("Book id to remove: "));
                        System.out.println("Book removed.");
                        break;
                    case 3:
                        String name = readLine("Member name: ");
                        String type = readLine("Type (S = Student, T = Teacher): ");
                        User u = library.registerUser(name, type.equalsIgnoreCase("T"));
                        System.out.println("Registered: " + u);
                        break;
                    case 4:
                        library.issueBook(readInt("Member id: "), readInt("Book id: "));
                        System.out.println("Book issued successfully.");
                        break;
                    case 5:
                        library.returnBook(readInt("Member id: "), readInt("Book id: "));
                        System.out.println("Book returned successfully.");
                        break;
                    case 6:
                        List<Book> found = library.search(readLine("Search keyword: "));
                        if (found.isEmpty()) System.out.println("No books found.");
                        else found.forEach(System.out::println);
                        break;
                    case 7:
                        library.getAllBooks().forEach(System.out::println);
                        break;
                    case 8:
                        List<Book> issued = library.getIssuedBooks();
                        if (issued.isEmpty()) System.out.println("No books are issued.");
                        else issued.forEach(System.out::println);
                        break;
                    case 9:
                        library.getAllUsers().forEach(System.out::println);
                        break;
                    case 0:
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice, try again.");
                }
            } catch (LibraryException e) {
               
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n===== LIBRARY MENU =====");
        System.out.println("1. Add book");
        System.out.println("2. Remove book");
        System.out.println("3. Register member");
        System.out.println("4. Issue book");
        System.out.println("5. Return book");
        System.out.println("6. Search books");
        System.out.println("7. List all books");
        System.out.println("8. List issued books");
        System.out.println("9. List members");
        System.out.println("0. Exit");
    }

    private static String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    
    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
}
