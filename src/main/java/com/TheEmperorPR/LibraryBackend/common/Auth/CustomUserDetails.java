package com.TheEmperorPR.LibraryBackend.common.Auth;

import com.TheEmperorPR.LibraryBackend.common.domain.enums.Role;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;


public class CustomUserDetails implements UserDetails {

    @Getter
    private final Long id;

    @Getter
    private final String username;

    @Getter
    private final String password;

    @Getter
    private final Role role;



    public CustomUserDetails(Long id, String password, String username, Role role) {
        this.id = id;
        this.password = password;
        this.username = username;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
       return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public boolean isAccountNonExpired() {return true;}

    @Override
    public boolean isAccountNonLocked(){ return true; }

    @Override
    public boolean isCredentialsNonExpired(){ return true;}

    @Override
    public boolean isEnabled(){ return true; }

}
