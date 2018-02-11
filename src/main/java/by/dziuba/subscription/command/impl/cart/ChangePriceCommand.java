package by.dziuba.subscription.command.impl.cart;

import by.dziuba.subscription.command.Command;
import by.dziuba.subscription.command.CommandResult;
import by.dziuba.subscription.command.JspResourceManager;
import by.dziuba.subscription.command.RequestContent;
import by.dziuba.subscription.command.exception.CommandException;
import by.dziuba.subscription.service.PeriodicalService;
import by.dziuba.subscription.service.exception.ServiceException;
import by.dziuba.subscription.service.impl.PeriodicalServiceImpl;

import java.math.BigDecimal;
import java.util.Map;

public class ChangePriceCommand implements Command {
    private PeriodicalService periodicalService = new PeriodicalServiceImpl();

    @Override
    public CommandResult execute(RequestContent requestContent) throws CommandException {
        CommandResult commandResult = new CommandResult(JspResourceManager.CART_PAGE_COMMAND);

        Map<Integer, Integer> quantities = (Map<Integer, Integer>)requestContent.getSessionAttribute("quantities");
        Integer id = Integer.parseInt(requestContent.getRequestParameter("id"));
        int quantity = quantities.get(id);
        quantities.put(id, Integer.parseInt(requestContent.getRequestParameter("quantity")));
        BigDecimal totalPrice = calculateTotalPrice(requestContent, id, quantity, quantities);

        commandResult.putSessionAttribute("totalPrice", totalPrice);
        return commandResult;
    }

    private BigDecimal calculateTotalPrice(RequestContent requestContent, Integer id, int quantity,
                                           Map<Integer, Integer> quantities) throws CommandException {
        try {
            BigDecimal price = periodicalService.getByPeriodicalId(id).getPrice();
            BigDecimal oldSubtotal = price.multiply(new BigDecimal(quantity));
            BigDecimal newSubtotal = price.multiply(new BigDecimal(quantities.get(id)));
            BigDecimal totalPrice = (BigDecimal)requestContent.getSessionAttribute("totalPrice");
            totalPrice = totalPrice.subtract(oldSubtotal);
            totalPrice = totalPrice.add(newSubtotal);
            return totalPrice;
        } catch (ServiceException e) {
            throw new CommandException(e);
        }
    }
}
