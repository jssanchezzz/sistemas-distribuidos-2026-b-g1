package co.edu.corhuila.opti.sales.authz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Proxy;
import java.util.Set;

import org.junit.jupiter.api.Test;

import co.edu.corhuila.opti.sales.adapter.in.http.ApiException;
import co.edu.corhuila.opti.sales.adapter.in.http.AuthenticatedUser;
import co.edu.corhuila.opti.sales.adapter.in.http.ErrorCode;
import co.edu.corhuila.opti.sales.adapter.in.http.RequestRules;
import co.edu.corhuila.opti.sales.adapter.in.http.Roles;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Story: only ADMIN or SELLER may register a payment in opti-sales-api.
 * Endpoint: POST /api/v1/invoices/{id}/payments, guarded in InvoiceController by
 * RequestRules.requireRole(http, Roles.ADMIN, Roles.SELLER). These tests call that same real rule.
 */
class PaymentsAuthzTest {

    @Test
    void optometristTokenCannotRegisterAPayment() {
        HttpServletRequest request = requestOf(new AuthenticatedUser("optometrist-1", Set.of(Roles.OPTOMETRIST)));

        ApiException refused = assertThrows(ApiException.class,
                () -> RequestRules.requireRole(request, Roles.ADMIN, Roles.SELLER));

        assertEquals(ErrorCode.FORBIDDEN, refused.code());
    }

    @Test
    void sellerTokenCanRegisterAPayment() {
        AuthenticatedUser seller = new AuthenticatedUser("seller-1", Set.of(Roles.SELLER));

        assertSame(seller, RequestRules.requireRole(requestOf(seller), Roles.ADMIN, Roles.SELLER));
    }

    private static HttpServletRequest requestOf(AuthenticatedUser user) {
        return (HttpServletRequest) Proxy.newProxyInstance(HttpServletRequest.class.getClassLoader(),
                new Class<?>[] {HttpServletRequest.class},
                (proxy, method, args) -> method.getName().equals("getAttribute") ? user : null);
    }
}
