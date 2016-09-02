package com.gft.digitalbank.exchange.solution.transactions;

import com.gft.digitalbank.exchange.model.OrderDetails;
import com.gft.digitalbank.exchange.model.Transaction;
import com.gft.digitalbank.exchange.model.orders.PositionOrder;
import com.gft.digitalbank.exchange.solution.matcher.BuySellResult;
import com.gft.digitalbank.exchange.solution.transactions.repositories.TransactionRepository;
import junitparams.JUnitParamsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;

import static com.gft.digitalbank.exchange.model.orders.Side.BUY;
import static com.gft.digitalbank.exchange.model.orders.Side.SELL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(JUnitParamsRunner.class)
public class TransactionServiceImplTest {
    @Mock
    BuySellResult result;
    @Mock
    TransactionRepository repository;
    @InjectMocks
    TransactionServiceImpl transactionServiceImpl;
    @Captor
    ArgumentCaptor<Transaction> argCap;

    public TransactionServiceImplTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void init() throws Exception {
        reset(result);
    }

    @Test
    public void shouldPopulateTransactionsId() {
        String product = "ABC";
        PositionOrder buyOrder = PositionOrder.builder().id(1).broker("Buy Broker").side(BUY).product(product).timestamp(1).client("Buy Client").details(OrderDetails.builder().price(1).amount(1).build()).build();
        PositionOrder sellOrder = PositionOrder.builder().id(1).broker("Sell Broker").side(SELL).product(product).timestamp(2).client("Sell Client").details(OrderDetails.builder().price(1).amount(1).build()).build();

        BuySellResult result = BuySellResult.builder().amount(1).price(1).buyOrder(buyOrder).sellOrder(sellOrder).buyFullyMatched(true).productName(product).build();

        transactionServiceImpl.save(result);
        verify(repository).save(argCap.capture());

        assertEquals(1, argCap.getValue().getId());

    }

    @Test
    public void shouldNotReturnNullTransactionSet() {
        assertNotNull(transactionServiceImpl.retrieveTransactions());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenResultIsNull() {
        transactionServiceImpl.save(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenAmountIsNegative() {
        when(result.getAmount()).thenReturn(-1);

        transactionServiceImpl.save(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenAmountIsZero() {
        when(result.getAmount()).thenReturn(0);

        transactionServiceImpl.save(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenPriceIsNegative() {
        when(result.getAmount()).thenReturn(1);
        when(result.getPrice()).thenReturn(-1);

        transactionServiceImpl.save(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenPriceIsZero() {
        when(result.getAmount()).thenReturn(1);
        when(result.getPrice()).thenReturn(0);

        transactionServiceImpl.save(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenProductIsNull() {
        when(result.getAmount()).thenReturn(1);
        when(result.getPrice()).thenReturn(1);
        when(result.getProductName()).thenReturn(null);

        transactionServiceImpl.save(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenProductIsEmpty() {
        when(result.getAmount()).thenReturn(1);
        when(result.getPrice()).thenReturn(1);
        when(result.getProductName()).thenReturn("");

        transactionServiceImpl.save(result);
    }
}
