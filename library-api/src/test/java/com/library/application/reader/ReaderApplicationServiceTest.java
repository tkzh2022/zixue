package com.library.application.reader;

import com.library.application.reader.command.CreateReaderCommand;
import com.library.domain.reader.Reader;
import com.library.domain.reader.ReaderRepository;
import com.library.domain.reader.ReaderStatus;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import com.library.domain.user.UserAccount;
import com.library.domain.user.UserAccountRepository;
import com.library.domain.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReaderApplicationServiceTest {

    @Mock
    private ReaderRepository readerRepository;
    
    @Mock
    private UserAccountRepository userAccountRepository;

    private ReaderApplicationService readerApplicationService;

    @BeforeEach
    void setUp() {
        readerApplicationService = new ReaderApplicationService(readerRepository, userAccountRepository);
    }

    @Test
    void createReader_Success() {
        CreateReaderCommand cmd = new CreateReaderCommand(
                1L, "R001", "John Doe", "13800000000", "john@example.com", LocalDate.now()
        );

        when(readerRepository.existsByReaderNo(cmd.readerNo())).thenReturn(false);
        
        UserAccount userAccount = UserAccount.register("john", "hash", UserRole.READER, Instant.now());
        userAccount.assignId(1L);
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(userAccount));
        when(readerRepository.findByUserAccountId(1L)).thenReturn(Optional.empty());

        Reader mockSavedReader = Reader.create(1L, "R001", "John Doe", "13800000000", "john@example.com", LocalDate.now());
        mockSavedReader.assignId(100L);
        when(readerRepository.save(any(Reader.class))).thenReturn(mockSavedReader);

        Long id = readerApplicationService.createReader(cmd);

        assertEquals(100L, id);
        verify(readerRepository).save(any(Reader.class));
    }

    @Test
    void createReader_UserAccountNotReader_ThrowsException() {
        CreateReaderCommand cmd = new CreateReaderCommand(
                1L, "R001", "John Doe", "13800000000", "john@example.com", LocalDate.now()
        );

        when(readerRepository.existsByReaderNo(cmd.readerNo())).thenReturn(false);
        
        UserAccount userAccount = UserAccount.register("admin", "hash", UserRole.LIBRARIAN, Instant.now());
        userAccount.assignId(1L);
        when(userAccountRepository.findById(1L)).thenReturn(Optional.of(userAccount));

        BusinessException ex = assertThrows(BusinessException.class, () -> readerApplicationService.createReader(cmd));
        assertEquals(ResultCode.PARAM_INVALID, ex.resultCode());
        verify(readerRepository, never()).save(any(Reader.class));
    }
}
