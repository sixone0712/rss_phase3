package jp.co.canon.rss.logmanager.controller;

import jp.co.canon.rss.logmanager.dto.address.AddressBookDTO;
import jp.co.canon.rss.logmanager.dto.address.ReqAddAddressDTO;
import jp.co.canon.rss.logmanager.dto.address.ReqAddGroupDTO;
import jp.co.canon.rss.logmanager.dto.address.ReqEditGroupDTO;
import jp.co.canon.rss.logmanager.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/address")
public class AddressController {

	private final AddressBookService addressBookService;

	public AddressController(AddressBookService addressBookService) {
		this.addressBookService = addressBookService;
	}

	@GetMapping()
	public ResponseEntity<?> getGroupAndAddressList(HttpServletRequest request, HttpServletResponse response) {
		try {
			List<AddressBookDTO> list = addressBookService.getGroupAndAddressList();
			return ResponseEntity.status(HttpStatus.OK).body(list);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@GetMapping("/email")
	public ResponseEntity<?> getAddressList(HttpServletRequest request, HttpServletResponse response) {
		try {
			List<AddressBookDTO> list = addressBookService.getAddressListDto();
			return ResponseEntity.status(HttpStatus.OK).body(list);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@GetMapping("/email/{id}/group")
	public ResponseEntity<?> getGroupByEmail(
		HttpServletRequest request,
		HttpServletResponse response,
		@Valid @PathVariable(name = "id", required = true) @NotNull Long id
	) {
		try {
			List<AddressBookDTO> list = addressBookService.getGroupListByAddressId(id);
			return ResponseEntity.status(HttpStatus.OK).body(list);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@DeleteMapping("/email")
	public ResponseEntity<?> deleteAddress(
		HttpServletRequest request,
		HttpServletResponse response,
		@Valid @RequestParam(name = "ids", required = false) @NotNull List<Long> ids) {
		try {
			if (ObjectUtils.isEmpty(ids)) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			}
			addressBookService.deleteAddressList(ids);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@PostMapping("/email")
	public ResponseEntity<?> AddAddress(
		HttpServletRequest request,
		HttpServletResponse response,
		@Valid @RequestBody ReqAddAddressDTO reqData) {
		try {
			addressBookService.addAddress(reqData.getName(), reqData.getEmail(), reqData.getGroupIds());
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@PutMapping("/email/{id}")
	public ResponseEntity<?> editAddress(
		HttpServletRequest request,
		HttpServletResponse response,
		@Valid @PathVariable(name = "id", required = true) @NotNull Long id,
		@Valid @RequestBody ReqAddAddressDTO reqData) {
		try {
			addressBookService.modifyAddress(id, reqData.getName(), reqData.getEmail(), reqData.getGroupIds());
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@GetMapping("/group")
	public ResponseEntity<?> getGroupNameList(HttpServletRequest request, HttpServletResponse response) {
		try {
			List<AddressBookDTO> addressList = addressBookService.getGroupNameList();
			return ResponseEntity.status(HttpStatus.OK).body(addressList);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@GetMapping("/group/email/{id}")
	public ResponseEntity<?> getGroupEmailList(
		HttpServletRequest request,
		HttpServletResponse response,
		@Valid @PathVariable(name = "id", required = true) @NotNull Long id) {
		try {
			List<AddressBookDTO> list = addressBookService.getGroupMembersDto(id);

			return ResponseEntity.status(HttpStatus.OK).body(list);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@PostMapping("/group")
	public ResponseEntity<?> addGroup(
		HttpServletRequest request,
		HttpServletResponse response,
		@RequestBody @Valid ReqAddGroupDTO reqData) {
		try {
			addressBookService.addGroup(reqData.getName(), reqData.getEmailIds());
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@PutMapping("/group/{id}")
	public ResponseEntity<?> editGroup(
		HttpServletRequest request,
		HttpServletResponse response,
		@Valid @PathVariable(name = "id", required = true) @NotNull Long id,
		@RequestBody @Valid ReqEditGroupDTO reqData) {
		try {
			addressBookService.modifyGroup(id, reqData.getName(), reqData.getEmailIds());
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@DeleteMapping("/group/{id}")
	public ResponseEntity<?> deleteGroup(
		HttpServletRequest request,
		HttpServletResponse response,
		@Valid @PathVariable(name = "id", required = true) @NotNull Long id) {
		try {
			addressBookService.deleteGroup(id);
			return ResponseEntity.status(HttpStatus.OK).body(null);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@GetMapping("/search")
	public ResponseEntity<?> searchAddressAndGroup(
		HttpServletRequest request,
		HttpServletResponse response,
		@Valid @RequestParam(name = "keyword", required = false, defaultValue = "") String keyword,
		@Valid @RequestParam(name = "group", required = false, defaultValue = "false") Boolean withGroup) {
		try {
			List<AddressBookDTO> list = addressBookService.search(keyword, withGroup);
			return ResponseEntity.status(HttpStatus.OK).body(list);
		} catch (ResponseStatusException e) {
			throw e;
		} catch (Exception e) {
			log.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
}
