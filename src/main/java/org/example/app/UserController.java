package org.example.app;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private PictureRepository pictureRepository;
    @Autowired
    private MessageRepository messageRepository;

    private List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png");

    // Maximum file size (120 KB)
    private long maxFileSize = 120000;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    @PostMapping("/register")
    public String processRegistration(@ModelAttribute("user") User user) {
        userService.saveUser(user);
        return "redirect:/success";

    }

    @GetMapping("/success")
    public String registrationSuccess(Model model) {
        model.addAttribute("user", new User());
        return "success"; // Show success page
    }
    @GetMapping("/successful")
    public String registrationSuccessful(Model model) {
        model.addAttribute("user", new User());
        return "successful"; // Show success page
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/profile")
    public String userProfile(@RequestParam(name = "userId") Long userId,
                              Model model) {
        User user = userService.getUserById(userId);
        List<Picture> pictures = pictureRepository.findByUserId(userId);
        if (user == null) {
            // Handle case where user is not found
            model.addAttribute("errorMessage", "User not found with ID: " + userId);
            return "error"; // You can redirect to an error page or handle differently
        }


            model.addAttribute("user", user);
        model.addAttribute("pictures",pictures);
        return "profile";
    }

    @PostMapping("/upload/{userId}")
    public String addPicture(@PathVariable Long userId, @RequestParam("file") MultipartFile file,
                             @RequestParam(value = "mainPicture", required = false, defaultValue = "false") boolean mainPicture,
                             RedirectAttributes redirectAttributes) throws IOException{
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:/success";
        }

        try {
            byte[] imageData = file.getBytes();
            userService.addPicture(userId, imageData, mainPicture);
            redirectAttributes.addFlashAttribute("message", "Picture uploaded successfully");
        } catch (IOException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("message", "Failed to upload picture");
        }

        return "redirect:/success";
    }


    @PostMapping("/deletePicture/{userId}/{pictureId}")
    public String deletePicture(@PathVariable Long userId, @PathVariable Long pictureId, RedirectAttributes redirectAttributes) {
        try {
            userService.removePicture(userId, pictureId);
            redirectAttributes.addFlashAttribute("message", "Picture deleted successfully");
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/successful";
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestParam("senderId") Long senderId,
                                              @RequestParam("receivedId") Long receivedId,
                                              @RequestParam("content") String content) {
        User sender = userService.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid sender ID"));

        User received = userService.findById(receivedId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid recipient ID"));

        Message message = new Message();
        message.setSender(sender);
        message.setReceived(received);
        message.setContent(content);
        message.setSentTime(LocalDateTime.now());

        messageRepository.save(message);

        return ResponseEntity.ok("Message sent successfully");
    }

    @GetMapping("/send")
    public String sendMessage(Model model) {
        //   model.addAttribute("message", new Message());
        return "send.html"; // Show success page
    }
}
