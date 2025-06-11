import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.catalog.Catalog;

import com.apple.laf.resources.aqua;

enum BookStatus{ AVAILABLE, RESERVED, LOST};
enum ReservationStatus{ WAITING, PENDING, COMPLETED, CANCELLED, NONE}
enum AccountStatus { ACTIVE, CLOSED, CANCELLED, BLACKLISTED}
enum BookFormat { HARDCOVER, PAPERBACK, EBOOK}

class Book{
    String id;
    String name;
    String subject;
    String publisher;
    Author authors;
    BookFormat format;

    public Book(String id, String name, String subject, String publisher, Author author, BookFormat fomrat){
        this.id=id;
        this.name=name;
        this.subject=subject;
        this.publisher=publisher;
        this.authors=author;
        this.format=format;
    }
}

class Author{
    String name;
}

class BookItem {
    private final Book book;
    private final String barcode;
    BookStatus status;
    Date dueDate;
    Member issuedTo;

    public BookItem(Book book, String barcode){
        this.book=book;
        this.barcode=barcode;
        this.status=BookStatus.AVAILABLE;
    }

    public boolean isAvailable(){
        return status == BookStatus.AVAILABLE;
    }

    public void markAsIssued(Member member){
        this.status=BookStatus.RESERVED;
        this.issuedTo=member;
        this.dueDate=new Date(System.currentTimeMillis()+14*86400000L);
    }

    public void markAsReturned(){
        this.status=BookStatus.AVAILABLE;
        this.issuedTo=null;
        this.dueDate=null;
    }

    public Date getDueDate() {
        return Date();
    }
}

class Member extends Account{
    Person person;
    Date dateOfMembership;
    private final List<BookItem> checkouItems = new ArrayList<>();
    public static final int MAX_BOOKS = 10;

    public Member(String id, String name, String email, String phone){
        super(id, name, email, phone);
    }
    public List<BookItem> getCheckOutBooks() {
        return checkouItems;
    }
    public void addBook(BookItem item) {
        checkouItems.add(item);
    }
    public void removeBook(BookItem book) {
            checkouItems.remove(book);
     }
}

class Person{
    String id;
    String name;
    String email;
    String address;
}

class Account{
    String id;
    String name;
    String email;
    String phone;
    AccountStatus status;


    public Account(String id, String name, String email, String phone){
        this.id=id;
        this.name=name;
        this.email=email;
        this.phone=phone;
        this.status=AccountStatus.ACTIVE;
    }
}

//===interface
interface Catalog{
    List<BookItem> getByTitle(String title);
    List<BookItem> getByAuthor(String Author);
}

//===interface

interface SearchStrategy{
    List<BookItem> search(Catalog catalog, String query);

}

class SearchByTitle implements SearchStrategy{
    public List<BookItem> search(Catalog catalog, String title){
        return catalog.getByTitle(title);
    }
}

class SearchByAuthor implements SearchStrategy{
    public List<BookItem> search(Catalog catalog, String author){
        return catalog.getByAuthor(author);
    }
}

//===interface
interface FineCalculationStrategy{
    double calculateFine(Date dueDate, Date returnDate);
}

class DefaultFineCalculation implements FineCalculationStrategy{
    private static final double daily_fine = 1.5;
     public double calculateFine(Date due, Date returned){
        long diff = returned.getTime() - due.getTime();
        long days = TimeUnit.MICROSECONDS.toDays(diff);
        return days>0?days*daily_fine : 0;
     }
}

//===service


class CheckoutService{
    private final FineCalculationStrategy fineCalculationStrategy;

    public CheckoutService(FineCalculationStrategy fineCalculationStrategy){
        this.fineCalculationStrategy=fineCalculationStrategy;
    }

    public boolean checkout(BookItem item, Member member){
        if(!item.isAvailable() || member.getCheckOutBooks().size()>member.MAX_BOOKS) return false;
        item.markAsIssued(member);
        member.addBook(item);
        return true;
    }

    public boolean returnBook(BookItem book, Member member){
        if(!member.getCheckOutBooks().contains(book)) return false;
        Date today = new Date(System.currentTimeMillis());
        Date dueDate = book.getDueDate();
        double fine = fineCalculationStrategy.calculateFine(dueDate, today);
        book.markAsReturned();
        member.removeBook(book);
        return true;
        
    }
}
