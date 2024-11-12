package com.sparta.project.service;


import com.sparta.project.domain.Address;
import com.sparta.project.domain.User;
import com.sparta.project.dto.address.AddressCreateRequest;
import com.sparta.project.dto.address.AddressUpdateRequest;
import com.sparta.project.exception.CodeBloomException;
import com.sparta.project.exception.ErrorCode;
import com.sparta.project.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserService userService;
    private final AddressRepository addressRepository;


    @Transactional
    public void createAddress(final long userId, final AddressCreateRequest request) {
        User user = userService.getUserOrException(userId);
        if(overMaxAddress(user)) {
            throw new CodeBloomException(ErrorCode.EXCEED_MAXIMUM_ADDRESS);
        }
        if(request.isDefault() && alreadyExistDefault(user)) {
            changeDefaultAddress(user);
        }
        addressRepository.save(Address.create(
                user, request.city(), request.district(), request.streetName(),
                request.streetNumber(), request.detail(), request.isDefault()
        ));
    }

    @Transactional
    public void updateAddress(long userId, String addressId, AddressUpdateRequest request) {
        User user = userService.getUserOrException(userId);
        Address address = getAddressOrException(addressId);
        checkAddressOwner(user, address.getUser());
        if(request.isDefault() && alreadyExistDefault(user)) {
            changeDefaultAddress(user);
        }
        address.update(request.city(), request.district(), request.streetName(),
                request.streetNumber(), request.detail(), request.isDefault()
        );
    }

    @Transactional
    public void deleteAddress(long userId, String addressId) {
        User user = userService.getUserOrException(userId);
        Address address = getAddressOrException(addressId);
        checkAddressOwner(user, address.getUser());
        address.deleteBase(String.valueOf(userId));
    }

    private boolean overMaxAddress(final User user) {
        return addressRepository.countByUser(user) > 5;
    }

    private boolean alreadyExistDefault(final User user) {
        return addressRepository.existsByUserAndIsDefault(user, true);
    }

    private void changeDefaultAddress(User user) {
        Address defaultAddress = addressRepository.findByUserAndIsDefault(user, true);
        defaultAddress.updateDefault(false);
    }

    private void checkAddressOwner(User requestUser, User ownerUser) {
        if(requestUser != ownerUser) {
            throw new CodeBloomException(ErrorCode.FORBIDDEN_ACCESS);
        }
    }

    private Address getAddressOrException(final String addressId) {
        return addressRepository.findById(addressId).orElseThrow(()->
                new CodeBloomException(ErrorCode.ADDRESS_NOT_FOUND)
        );
    }

}
