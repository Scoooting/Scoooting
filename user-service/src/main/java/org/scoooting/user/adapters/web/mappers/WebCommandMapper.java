package org.scoooting.user.adapters.web.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.scoooting.user.adapters.web.dto.*;
import org.scoooting.user.application.dto.request.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface WebCommandMapper {

    SignInCommand toSignInCommand(UserSignInDto signInDto);

    RegistrationCommand toRegistrationCommand(UserRegistrationDTO userRegistrationDTO);

    CreateUserByAdminCommand toCreateUserByAdminCommand(UserCreationByAdminRequestDTO requestDTO);

    AdminUpdateUserCommand toAdminUpdateUserCommand(AdminUpdateUserRequestDTO requestDTO);

    UpdateUserCommand toUpdateUserCommand(UpdateUserRequestDTO requestDTO);
}
