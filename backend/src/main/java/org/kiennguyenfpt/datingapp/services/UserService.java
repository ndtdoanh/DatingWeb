package org.kiennguyenfpt.datingapp.services;

import org.kiennguyenfpt.datingapp.dtos.requests.UpdateProfileRequest;
import org.kiennguyenfpt.datingapp.dtos.responses.AdminUserResponse;
import org.kiennguyenfpt.datingapp.dtos.responses.NearlyUserResponse;
import org.kiennguyenfpt.datingapp.entities.User;
import org.kiennguyenfpt.datingapp.entities.UserLocation;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {

    User updateProfile(String email, UpdateProfileRequest updateProfileRequest, List<MultipartFile> files) throws IOException;

    User findByEmail(String email);

    User save(User user);

    List<AdminUserResponse> searchAdminUsers(String keyword);

    List<User> searchUsers(String keyword);

    int lockOrUnLockUser(Long id);

    User findById(Long id);

    List<NearlyUserResponse> findNearbyUsers(UserLocation currentLocation, double range);
}
