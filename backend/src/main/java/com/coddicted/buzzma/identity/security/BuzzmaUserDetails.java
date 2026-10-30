package com.coddicted.buzzma.identity.security;

import com.coddicted.buzzma.identity.entity.BuzzmaUser;
import com.coddicted.buzzma.identity.entity.UserRole;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class BuzzmaUserDetails implements UserDetails {

  private final BuzzmaUser user;

  /** The admin viewing as {@link #user}; {@code null} outside view-as mode. */
  private final BuzzmaUser actor;

  public BuzzmaUserDetails(BuzzmaUser user) {
    this(user, null);
  }

  public BuzzmaUserDetails(BuzzmaUser user, BuzzmaUser actor) {
    this.user = user;
    this.actor = actor;
  }

  public BuzzmaUser getUser() {
    return this.user;
  }

  public BuzzmaUser getActor() {
    return this.actor;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    final UserRole userRole = user.getRole();
    return List.of(new SimpleGrantedAuthority(userRole.name()));
  }

  @Override
  public String getPassword() {
    return "hashed";
  }

  @Override
  public String getUsername() {
    return user.getMobile();
  }

  @Override
  public boolean isEnabled() {
    return !user.getIsDeleted();
  }
}
