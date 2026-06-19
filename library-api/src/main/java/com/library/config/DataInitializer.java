package com.library.config;

import com.library.domain.user.UserAccount;
import com.library.domain.user.UserAccountRepository;
import com.library.domain.user.UserRole;
import com.library.domain.book.Book;
import com.library.domain.book.BookRepository;
import com.library.domain.book.BookCopy;
import com.library.domain.reader.Reader;
import com.library.domain.reader.ReaderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Component
@Profile("dev")
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;

    public DataInitializer(UserAccountRepository userAccountRepository,
                           PasswordEncoder passwordEncoder,
                           BookRepository bookRepository,
                           ReaderRepository readerRepository) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.bookRepository = bookRepository;
        this.readerRepository = readerRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!userAccountRepository.existsByUsername("librarian")) {
            log.info("Initializing default librarian account...");
            String hash = passwordEncoder.encode("librarian123");
            UserAccount account = UserAccount.register("librarian", hash, UserRole.LIBRARIAN, Instant.now());
            userAccountRepository.save(account);
        }

        if (!userAccountRepository.existsByUsername("reader1")) {
            log.info("Initializing default reader account...");
            String hash = passwordEncoder.encode("reader123");
            UserAccount account = UserAccount.register("reader1", hash, UserRole.READER, Instant.now());
            userAccountRepository.save(account);
        }

        userAccountRepository.findByUsername("reader1").ifPresent(account -> {
            if (readerRepository.findByUserAccountId(account.id()).isEmpty()) {
                readerRepository.save(Reader.create(
                        account.id(), "R" + String.format("%08d", account.id()), "Reader One", "13800000000",
                        "reader1@example.com", LocalDate.now()));
            }
        });

        if (bookRepository.findAll().isEmpty()) {
            log.info("Initializing default books...");
            createBookWithCopy("978-0134685991", "Effective Java", "Addison-Wesley", 2017, "A-01", "Joshua Bloch", "COPY-001");
            createBookWithCopy("978-0201633610", "Design Patterns", "Addison-Wesley", 1994, "A-02", "Erich Gamma", "COPY-002");
            createBookWithCopy("978-0132350884", "Clean Code", "Prentice Hall", 2008, "A-03", "Robert C. Martin", "COPY-003");
            createBookWithCopy("978-0321356680", "Effective C++", "Addison-Wesley", 2005, "A-04", "Scott Meyers", "COPY-004");
            createBookWithCopy("978-0596007126", "Head First Design Patterns", "O'Reilly", 2004, "A-05", "Eric Freeman", "COPY-005");
        }
    }

    private void createBookWithCopy(String isbn, String title, String publisher, int year,
                                    String location, String author, String barcode) {
        Instant now = Instant.now();
        Book book = Book.create(isbn, title, publisher, year, location,
                "Sample catalog entry", java.util.List.of(author), java.util.List.of("Programming"), now);
        bookRepository.save(book);
        BookCopy copy = BookCopy.create(book.id(), barcode, now);
        book.addCopy();
        bookRepository.saveCopy(copy);
        bookRepository.save(book);
    }
}
