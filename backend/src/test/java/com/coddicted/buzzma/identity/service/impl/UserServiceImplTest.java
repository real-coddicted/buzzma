package com.coddicted.buzzma.identity.service.impl;

import static com.coddicted.buzzma.identity.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

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
    doReturn(Optional.of(USER_2)).when(this.mockUsersRepository).findById(USER_ID);

    final BuzzmaUser result = this.userService.getById(USER_ID);

    assertEquals(USER_2, result);
  }

  @Test
  void testGetByIdWhenNotFound() {
    doReturn(Optional.empty()).when(this.mockUsersRepository).findById(USER_ID);

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.userService.getById(USER_ID));
    assertEquals("Users not found: " + USER_ID, ex.getMessage());
  }

  @Test
  void testCreate() {
    doReturn(EXPECTED_USER_1).when(this.mockUsersRepository).save(USER_1);

    final BuzzmaUser result = this.userService.create(USER_1);

    assertEquals(EXPECTED_USER_1, result);
    verify(this.mockUsersRepository).save(USER_1);
  }

  @Test
  void testUpdateWhenFound() {
    doReturn(Optional.of(USER_2)).when(this.mockUsersRepository).findById(USER_ID);
    doReturn(EXPECTED_USER_2).when(this.mockUsersRepository).save(EXPECTED_USER_2);

    final BuzzmaUser result = this.userService.update(EXPECTED_USER_2);

    assertEquals(EXPECTED_USER_2, result);
    verify(this.mockUsersRepository).save(EXPECTED_USER_2);
  }

  @Test
  void testUpdateWhenNotFound() {
    doReturn(Optional.empty()).when(this.mockUsersRepository).findById(USER_ID);

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.userService.update(EXPECTED_USER_2));
    assertEquals("Users not found: " + USER_ID, ex.getMessage());
  }

  @Test
  void testGetByMobileWhenFound() {
    doReturn(Optional.of(USER_2))
        .when(this.mockUsersRepository)
        .findByMobileAndIsDeletedFalse(MOBILE);

    final BuzzmaUser result = this.userService.getByMobile(MOBILE);

    assertEquals(USER_2, result);
  }

  @Test
  void testGetByMobileWhenNotFound() {
    doReturn(Optional.empty()).when(this.mockUsersRepository).findByMobileAndIsDeletedFalse(MOBILE);

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.userService.getByMobile(MOBILE));
    assertEquals("user not found: " + MOBILE, ex.getMessage());
  }

  @Test
  void testDeleteWhenFound() {
    doReturn(Optional.of(USER_2)).when(this.mockUsersRepository).findById(USER_ID);

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
    doReturn(Optional.empty()).when(this.mockUsersRepository).findById(USER_ID);

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.userService.delete(USER_ID, REQUESTER_ID));
    assertEquals("Users not found: " + USER_ID, ex.getMessage());
  }

  @Test
  void testExistsByMobileWhenUserExists() {
    doReturn(true).when(this.mockUsersRepository).existsUserByMobileAndIsDeletedFalse(MOBILE);

    assertTrue(this.userService.existsByMobile(MOBILE));
  }

  @Test
  void testExistsByMobileWhenUserDoesNotExist() {
    doReturn(false).when(this.mockUsersRepository).existsUserByMobileAndIsDeletedFalse(MOBILE);

    assertFalse(this.userService.existsByMobile(MOBILE));
  }
}
