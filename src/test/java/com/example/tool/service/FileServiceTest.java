package com.example.tool.service;

import com.example.tool.entity.FileMetadata;
import com.example.tool.exception.InvalidDataException;
import com.example.tool.exception.ResourceNotFoundException;
import com.example.tool.repository.FileMetadataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @InjectMocks
    private FileService fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(fileService, "uploadDir", tempDir.toString());
        fileService.init();
    }

    @Test
    @DisplayName("should upload valid file successfully")
    void upload_success() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "dummy content".getBytes());

        when(fileMetadataRepository.save(any(FileMetadata.class))).thenAnswer(i -> i.getArgument(0));

        FileMetadata result = fileService.upload(file);

        assertNotNull(result);
        assertEquals("test.pdf", result.getOriginalName());
        assertEquals("application/pdf", result.getFileType());
        verify(fileMetadataRepository).save(any(FileMetadata.class));
    }

    @Test
    @DisplayName("should throw exception for invalid file type")
    void upload_invalidType_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.exe", "application/x-msdownload", "dummy content".getBytes());

        assertThrows(InvalidDataException.class, () -> fileService.upload(file));
    }

    @Test
    @DisplayName("should throw exception for empty file")
    void upload_emptyFile_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", new byte[0]);

        assertThrows(InvalidDataException.class, () -> fileService.upload(file));
    }

    @Test
    @DisplayName("should throw exception for large file")
    void upload_largeFile_throwsException() {
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11 MB
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", largeContent);

        assertThrows(InvalidDataException.class, () -> fileService.upload(file));
    }

    @Test
    @DisplayName("should get metadata successfully")
    void getMetadata_success() {
        FileMetadata metadata = new FileMetadata();
        metadata.setId(1L);
        when(fileMetadataRepository.findById(1L)).thenReturn(Optional.of(metadata));

        FileMetadata result = fileService.getMetadata(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("should throw exception when metadata not found")
    void getMetadata_notFound_throwsException() {
        when(fileMetadataRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> fileService.getMetadata(99L));
    }
}
