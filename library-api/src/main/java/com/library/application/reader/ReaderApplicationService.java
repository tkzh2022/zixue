package com.library.application.reader;

import com.library.application.reader.command.CreateReaderCommand;
import com.library.application.reader.command.UpdateReaderCommand;
import com.library.domain.reader.Reader;
import com.library.domain.reader.ReaderRepository;
import com.library.domain.reader.ReaderStatus;
import com.library.domain.shared.exception.BusinessException;
import com.library.domain.shared.exception.ResultCode;
import com.library.domain.user.UserAccount;
import com.library.domain.user.UserAccountRepository;
import com.library.domain.user.UserRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReaderApplicationService {

    private final ReaderRepository readerRepository;
    private final UserAccountRepository userAccountRepository;

    public ReaderApplicationService(ReaderRepository readerRepository, UserAccountRepository userAccountRepository) {
        this.readerRepository = readerRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public Long createReader(CreateReaderCommand cmd) {
        if (readerRepository.existsByReaderNo(cmd.readerNo())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "Reader No already exists");
        }

        UserAccount userAccount = userAccountRepository.findById(cmd.userAccountId())
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        if (userAccount.role() != UserRole.READER) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "User account is not a READER");
        }

        if (readerRepository.findByUserAccountId(cmd.userAccountId()).isPresent()) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "User account already has a reader profile");
        }

        Reader reader = Reader.create(
                cmd.userAccountId(),
                cmd.readerNo(),
                cmd.name(),
                cmd.phone(),
                cmd.email(),
                cmd.registerDate()
        );

        return readerRepository.save(reader).id();
    }

    @Transactional
    public void updateReader(UpdateReaderCommand cmd) {
        Reader reader = readerRepository.findById(cmd.id())
                .orElseThrow(() -> new BusinessException(ResultCode.PARAM_INVALID, "Reader not found"));

        reader.updateInfo(cmd.name(), cmd.phone(), cmd.email());
        readerRepository.save(reader);
    }

    @Transactional
    public void updateStatus(Long readerId, ReaderStatus status) {
        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new BusinessException(ResultCode.PARAM_INVALID, "Reader not found"));

        if (status == ReaderStatus.ACTIVE) {
            reader.enable();
        } else if (status == ReaderStatus.DISABLED) {
            reader.disable();
        }

        readerRepository.save(reader);
    }
}
