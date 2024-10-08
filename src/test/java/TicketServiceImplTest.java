import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

    class TicketServiceImplTest {

        @Mock
        private TicketPaymentService paymentService;

        @Mock
        private SeatReservationService reservationService;

        @InjectMocks
        private TicketServiceImpl ticketService;

        @BeforeEach
        void setUp() {
            MockitoAnnotations.openMocks(this);
        }

        @Test
        void testValidTicketPurchase() {
            // Arrange
            TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 2);
            TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
            Long accountId = 1L;

            // Act
            ticketService.purchaseTickets(accountId, adultTicket, childTicket);

            // Assert
            verify(paymentService, times(1)).makePayment(accountId, 65); // 2 ADULT (50) + 1 CHILD (15)
            verify(reservationService, times(1)).reserveSeat(accountId, 3); // 2 ADULT + 1 CHILD = 3 seats
        }

        @Test
        void testInvalidAccountId() {
            // Arrange
            TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);

            // Act & Assert
            assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(0L, adultTicket));
        }

        @Test
        void testNoTicketsRequested() {
            // Act & Assert
            assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(1L));
        }

        @Test
        void testExceedMaxTicketsLimit() {
            // Arrange
            TicketTypeRequest adultTicket = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 26); // 26 tickets exceeds the limit
            Long accountId = 1L;

            // Act & Assert
            assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(accountId, adultTicket));
        }

        @Test
        void testChildTicketWithoutAdult() {
            // Arrange
            TicketTypeRequest childTicket = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 1);
            Long accountId = 1L;

            // Act & Assert
            assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(accountId, childTicket));
        }

        @Test
        void testInfantTicketWithoutAdult() {
            // Arrange
            TicketTypeRequest infantTicket = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
            Long accountId = 1L;

            // Act & Assert
            assertThrows(InvalidPurchaseException.class, () -> ticketService.purchaseTickets(accountId, infantTicket));
        }
    }

