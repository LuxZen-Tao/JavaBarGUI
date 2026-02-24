package com.luxzentao.javabar.core;

import java.util.List;

public interface CreditLineSelector {
    CreditLine select(List<CreditLine> options, double shortfall, String reason);
}
