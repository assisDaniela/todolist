package com.danielaassis.todolist.task;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.danielaassis.todolist.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.PackagePrivate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        UUID idUser = (UUID) request.getAttribute("idUser");
        taskModel.setIdUser(idUser);

        var currentDate = LocalDateTime.now();

        if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.badRequest().body("Data start or data end of the task is less than the current data");
        }

        if(taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.badRequest().body("Data start is greater than data end");
        }

        TaskModel taskCreated = this.taskRepository.save(taskModel);
        return ResponseEntity.ok().body(taskCreated);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        UUID idUser = (UUID) request.getAttribute("idUser");
        return this.taskRepository.findByIdUser(idUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {
        Optional<TaskModel> task = this.taskRepository.findById(id);

        UUID idUser = (UUID) request.getAttribute("idUser");
        if(task.isPresent()) {
            UUID taskIdUser = (UUID) task.get().getIdUser();
            if(!taskIdUser.equals(idUser)) {
                return ResponseEntity.badRequest().body("User has no permission to update this task")
            }
            
            // taskModel -> possible properties with null values given by the user
            // task -> to be update
            Utils.copyNonNullProperties(taskModel, task);
            TaskModel taskUpdated = this.taskRepository.save(task.get());
            return ResponseEntity.ok().body(taskUpdated);
        }

        return ResponseEntity.badRequest().body("Task not found");
    }
}
