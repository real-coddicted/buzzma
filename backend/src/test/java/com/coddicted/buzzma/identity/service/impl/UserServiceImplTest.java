package com.coddicted.buzzma.identity.service.impl;

import static com.coddicted.buzzma.identity.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.persistence.UsersRepository;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Mock private UsersRepository mockUsersRepository;
  private UserServiceImpl userService;

  @BeforeEach
  void setUp() {
    this.userService = new UserServiceImpl(this.mockUsersRepository);
  }

  @Test
  void testGetByIdWhenFound() {
    when(this.mockUsersRepository.findById(USER_ID)).thenReturn(Optional.of(USER_2));

    final BuzzmaUser result = this.userService.getById(USER_ID);

    assertEquals(USER_2, result);
  }

  @Test
  void testGetByIdWhenNotFound() {
    when(this.mockUsersRepository.findById(USER_ID)).thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.userService.getById(USER_ID));
    assertEquals("Users not found: " + USER_ID, ex.getMessage());
  }

  @Test
  void testCreate() {
    when(this.mockUsersRepository.save(USER_1)).thenReturn(EXPECTED_USER_1);

    final BuzzmaUser result = this.userService.create(USER_1);

    assertEquals(EXPECTED_USER_1, result);
    verify(this.mockUsersRepository).save(USER_1);
  }

  @Test
  void testUpdateWhenFound() {
    when(this.mockUsersRepository.findById(USER_ID)).thenReturn(Optional.of(USER_2));
    when(this.mockUsersRepository.save(EXPECTED_USER_2)).thenReturn(EXPECTED_USER_2);

    final BuzzmaUser result = this.userService.update(EXPECTED_USER_2);

    assertEquals(EXPECTED_USER_2, result);
    verify(this.mockUsersRepository).save(EXPECTED_USER_2);
  }

  @Test
  void testUpdateWhenNotFound() {
    when(this.mockUsersRepository.findById(USER_ID)).thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.userService.update(EXPECTED_USER_2));
    assertEquals("Users not found: " + USER_ID, ex.getMessage());
  }

  @Test
  void testGetByMobileWhenFound() {
    when(this.mockUsersRepository.findByMobileAndIsDeletedFalse(MOBILE))
        .thenReturn(Optional.of(USER_2));

    final BuzzmaUser result = this.userService.getByMobile(MOBILE);

    assertEquals(USER_2, result);
  }

  @Test
  void testGetByMobileWhenNotFound() {
    when(this.mockUsersRepository.findByMobileAndIsDeletedFalse(MOBILE))
        .thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.userService.getByMobile(MOBILE));
    assertEquals("user not found: " + MOBILE, ex.getMessage());
  }

  @Test
  void testDeleteWhenFound() {
    when(this.mockUsersRepository.findById(USER_ID)).thenReturn(Optional.of(USER_2));

    this.userService.delete(USER_ID, REQUESTER_ID);

    final ArgumentCaptor<BuzzmaUser> captor = ArgumentCaptor.forClass(BuzzmaUser.class);
    verify(this.mockUsersRepository).save(captor.capture());
    final BuzzmaUser saved = captor.getValue();
    assertEquals(USER_ID, saved.getId());
    assertTrue(saved.getIsDeleted());
    assertEquals(REQUESTER_ID, saved.getUpdatedBy());
  }

  @Test
  void testDeleteWhenNotFound() {
    when(this.mockUsersRepository.findById(USER_ID)).thenReturn(Optional.empty());

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.userService.delete(USER_ID, REQUESTER_ID));
    assertEquals("Users not found: " + USER_ID, ex.getMessage());
  }

  @Test
  void testExistsByMobileWhenUserExists() {
    when(this.mockUsersRepository.existsUserByMobileAndIsDeletedFalse(MOBILE)).thenReturn(true);

    assertTrue(this.userService.existsByMobile(MOBILE));
  }

  @Test
  void testExistsByMobileWhenUserDoesNotExist() {
    when(this.mockUsersRepository.existsUserByMobileAndIsDeletedFalse(MOBILE)).thenReturn(false);

    assertFalse(this.userService.existsByMobile(MOBILE));
  }
}
