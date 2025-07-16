package com.TheEmperorPR.LibraryBackend.common.Auth;

import com.TheEmperorPR.LibraryBackend.common.database.entity.User;
import com.TheEmperorPR.LibraryBackend.common.database.repository.credRepo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found"));

        return new CustomUserDetails(user.getId(),user.getUsername(),user.getPassword(),user.getRole());
    }


}
