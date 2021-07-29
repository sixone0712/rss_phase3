package jp.co.canon.rss.logmanager.controller;

import jp.co.canon.rss.logmanager.dto.auth.ReqSingUpDTO;
import jp.co.canon.rss.logmanager.dto.user.ReqChangePasswordDTO;
import jp.co.canon.rss.logmanager.dto.user.ReqChangeRoleDTO;
import jp.co.canon.rss.logmanager.dto.user.ResUserDTO;
import jp.co.canon.rss.logmanager.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/user")
public class UserController {

	private UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping()
	public ResponseEntity<?> getUsers(
		HttpServletRequest request, HttpServletResponse response) {

		try {
			List<ResUserDTO> users = userService.getUsers();
			return ResponseEntity.status(HttpStatus.OK).body(users);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping()
	public ResponseEntity<?> signUp(
		HttpServletRequest request,
		HttpServletResponse response,
		@Valid @RequestBody ReqSingUpDTO singUpInput) {

		try {
			this.userService.signUp(singUpInput);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteUser(
		HttpServletRequest request,
		HttpServletResponse response,
		@Valid @PathVariable(value = "id") @NotNull Integer inputId) {

		try {
			this.userService.deleteUser(inputId);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@PutMapping("/{id}/roles")
	public ResponseEntity<?> changeRoles(
		HttpServletRequest request,
		HttpServletResponse response,
		@Valid @PathVariable(value = "id") @NotNull Integer inputId,
		@Valid @RequestBody ReqChangeRoleDTO roleInput) {

		try {
			List<String> role = roleInput.getRoles();
			this.userService.changeRoles(inputId, role);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@PutMapping("/{id}/password")
	public ResponseEntity<?> changePassword(
		HttpServletRequest request,
		HttpServletResponse response,
		@Valid @PathVariable(value = "id") @NotNull Integer inputId,
		@Valid @RequestBody ReqChangePasswordDTO passwordInput) {

		try {
			this.userService.changePassword(inputId, passwordInput);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

}
