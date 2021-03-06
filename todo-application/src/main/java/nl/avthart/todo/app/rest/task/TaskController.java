package nl.avthart.todo.app.rest.task;

import java.security.Principal;

import javax.validation.Valid;

import nl.avthart.todo.app.domain.task.commands.CompleteTaskCommand;
import nl.avthart.todo.app.domain.task.commands.CreateTaskCommand;
import nl.avthart.todo.app.domain.task.commands.ModifyTitleCommand;
import nl.avthart.todo.app.domain.task.commands.StarTaskCommand;
import nl.avthart.todo.app.query.task.TaskEntry;
import nl.avthart.todo.app.query.task.TaskQueryRepository;
import nl.avthart.todo.app.rest.task.requests.CreateTaskRequest;
import nl.avthart.todo.app.rest.task.requests.ModifyTitleRequest;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author albert
 */
@RestController
public class TaskController {

	@Autowired
	private TaskQueryRepository taskQueryRepository;

	@Autowired
	private SimpMessageSendingOperations messagingTemplate;
	
	@Autowired
	private CommandGateway commandGateway;

	@RequestMapping(value = "/api/tasks", method = RequestMethod.GET)
	public @ResponseBody
	Page<TaskEntry> findlAll(Principal principal, @RequestParam(required = false, defaultValue = "false") boolean completed, Pageable pageable) {
		return taskQueryRepository.findByUsernameAndCompleted(principal.getName(), completed, pageable);
	}

	@RequestMapping(value = "/api/tasks", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	public void createTask(Principal principal, @RequestBody @Valid CreateTaskRequest request) {
		commandGateway.sendAndWait(new CreateTaskCommand(principal.getName(), request.getTitle()));
	}

	@RequestMapping(value = "/api/tasks/{identifier}/title", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void createTask(@PathVariable String identifier, @RequestBody @Valid ModifyTitleRequest request) {
		commandGateway.sendAndWait(new ModifyTitleCommand(identifier, request.getTitle()));
	}

	@RequestMapping(value = "/api/tasks/{identifier}/complete", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void createTask(@PathVariable String identifier) {
		commandGateway.sendAndWait(new CompleteTaskCommand(identifier));
	}

	@RequestMapping(value = "/api/tasks/{identifier}/star", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void starTask(@PathVariable String identifier) {
		commandGateway.sendAndWait(new StarTaskCommand(identifier));
	}

	@RequestMapping(value = "/api/tasks/{identifier}/unstar", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void unstarTask(@PathVariable String identifier) {
		throw new RuntimeException("Could not unstar task..."); 
		//commandGateway.sendAndWait(new UnstarTaskCommand(identifier));
	}

	@ExceptionHandler
	public void handleException(Principal principal, Throwable exception) {
		messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors", exception.getMessage());
	}

}