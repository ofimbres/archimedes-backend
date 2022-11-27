package com.binomiaux.archimedes.app.controller;

import com.binomiaux.archimedes.model.Classroom;
import com.binomiaux.archimedes.model.Exercise;
import com.binomiaux.archimedes.model.ExerciseResult;
import com.binomiaux.archimedes.model.Student;
import com.binomiaux.archimedes.service.ClassroomService;
import com.binomiaux.archimedes.service.ExerciseResultService;
import com.binomiaux.archimedes.service.ExerciseService;
import com.binomiaux.archimedes.service.StudentService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Exercise Results controller.
 */
@RestController
@RequestMapping("/api/v1/exerciseresult")
public class ExerciseResultsController {

    @Autowired
    private ExerciseResultService mExerciseResultService;
    @Autowired
    private StudentService mStudentService;
    @Autowired
    private ClassroomService mClassroomService;
    @Autowired
    private ExerciseService mExerciseService;

    @GetMapping("/class/{classroomId}/exercise/{exerciseId}")
    public ResponseEntity getByClassAndExercise(@PathVariable String classroomId, @PathVariable String exerciseId) {
        var exerciseResults = mExerciseResultService.getByClassAndExercise(classroomId, exerciseId);
        return ok(exerciseResults);
    }

    @GetMapping("/class/{classroomId}/student/{studentId}/exercise/{exerciseId}")
    public ResponseEntity getByClassStudentAndExercise(@PathVariable String classroomId, @PathVariable String studentId,
                                                       @PathVariable String exerciseId) {
        var exerciseResult = mExerciseResultService.getByStudentAndExercise(classroomId, studentId, exerciseId);
        return ok(exerciseResult);
    }

    @PostMapping("/")
    public ResponseEntity create(@RequestBody CreateExerciseResultRequest request) {
        ExerciseResult exerciseResult = new ExerciseResult();
        Student student = mStudentService.getStudent(request.getStudentId());
        Exercise exercise = mExerciseService.getExercise(request.getExerciseId());
        Classroom classRoom = mClassroomService.getClassroom(request.getClassroomId());

        exerciseResult.setExercise(exercise);
        exerciseResult.setStudent(student);
        exerciseResult.setClassroom(classRoom);
        exerciseResult.setScore(request.getScore());
        exerciseResult.setTimestamp(Instant.now());
        exerciseResult.setWorksheetContent(request.getWorksheetContentCopy());

        String id = UUID.randomUUID().toString();
        exerciseResult.setS3Key(id + ".html");

        mExerciseResultService.create(exerciseResult);
        return ok(exerciseResult);
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity download(@PathVariable String fileName) {
        List<ExerciseResult> exerciseResults = mExerciseResultService.getByClassAndExercise(
                "e46e7191-e31d-434a-aba3-b9a9c187a632", "WN16");
        String filename = "";
        /*ByteArrayOutputStream downloadInputStream = null;
        return ok()
                .contentType(MediaType.TEXT_HTML)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(downloadInputStream.toByteArray());*/
        return ok("potro");
    }

    @Data
    static class CreateExerciseResultRequest {
        private String worksheetContentCopy;
        private String exerciseId;
        private String classroomId;
        private String studentId;
        private int score;
    }
}
