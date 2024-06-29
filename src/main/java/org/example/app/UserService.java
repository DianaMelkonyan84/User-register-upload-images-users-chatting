package org.example.app;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PictureRepository pictureRepository;
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    public User saveUser(User user) {
        return userRepository.save(user);
    }
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
@Transactional
public void addPicture(Long userId, byte[] imageData, boolean mainPicture) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

    Picture picture = new Picture();
    picture.setImageData(imageData);
    picture.setUser(user);

    // Set mainPicture flag
    if (mainPicture) {
        // Clear previous main picture flag for the user
        user.getPictures().forEach(p -> p.setMainPicture(false));
        picture.setMainPicture(true);
    }

    user.getPictures().add(picture);
    userRepository.save(user);
}
    @Transactional
    public void removePicture(Long userId, Long pictureId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // Find the picture by id
        Picture pictureToRemove = user.getPictures().stream()
                .filter(p -> p.getId().equals(pictureId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Picture not found with id: " + pictureId));

        if (pictureToRemove.isMainPicture()) {
            throw new IllegalArgumentException("Cannot delete main picture");
        }

        // Remove the picture from user's pictures
        user.removePicture(pictureToRemove);
        pictureRepository.delete(pictureToRemove); // Delete the picture from the repository
        userRepository.save(user); // Save the user to update the changes
    }

}
