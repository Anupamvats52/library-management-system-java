# Library Management System (Java)

A small console app in Java for managing books and members. I built it to practise core Java and OOP.

It's a single file with no database, so data is kept in memory and resets when you exit.

## What it does

- Add and remove books
- Register members (student or teacher)
- Issue and return books
- Search by title or author
- List all books, issued books, and members

Students can hold 2 books at a time and teachers can hold 5. An issued book can't be issued again or removed. The app starts with 4 books and 2 members so you can try it right away.

## Running it

You need Java 8 or newer.

```
javac LibraryApp.java
java LibraryApp
```

## How it's built

- `Book`: id, title, author, availability
- `User` (abstract): shared fields for members
- `Student`, `Teacher`: extend `User`, each with its own borrow limit
- `Searchable`: interface with a `search()` method
- `Library`: holds the books and members and has the main logic
- `LibraryException` and two subclasses: custom errors like `BookNotAvailableException`
- `LibraryApp`: `main()` and the menu loop

OOP used: abstraction, inheritance, polymorphism (`getBorrowLimit()`), encapsulation, and custom exception handling. Books and members are stored in a `LinkedHashMap`.

## Limitations

- Data isn't saved between runs
- No due dates or fines
  
## ScreenShot
<img width="959" height="500" alt="Screenshot 2026-10-17 231917" src="https://github.com/user-attachments/assets/2a83bb7e-57d6-4d6f-9fc6-d532cda49326" />

 
