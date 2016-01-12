/**
 * Copyright (c) 2013-2015, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.seedstack.validation.internal.pojo;

import org.seedstack.seed.it.ITBind;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@ITBind
public class ParamReturnType {

    @NotNull
    public Object validateNotNullReturn(Object param) {
        return param;
    }

    @Valid
    public Pojo validateValidReturn(Pojo.State state) {
        return new Pojo(state);
    }
}